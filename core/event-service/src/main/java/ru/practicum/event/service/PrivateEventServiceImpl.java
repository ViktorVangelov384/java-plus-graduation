package ru.practicum.event.service;

import feign.FeignException;
import jakarta.validation.ValidationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.category.model.Category;
import ru.practicum.category.storage.CategoryRepository;
import ru.practicum.client.UserClient;
import ru.practicum.dto.user.UserShortDto;
import ru.practicum.enums.EventState;
import ru.practicum.event.dto.EventRequestDto;
import ru.practicum.event.dto.EventResponseDto;
import ru.practicum.dto.event.EventShortDto;
import ru.practicum.event.dto.EventUpdateRequestDto;
import ru.practicum.event.exception.ConditionsNotMetException;
import ru.practicum.event.exception.ConflictException;
import ru.practicum.event.exception.NotFoundException;
import ru.practicum.event.mapper.EventMapper;
import ru.practicum.event.model.Event;
import ru.practicum.event.model.EventLocation;
import ru.practicum.event.storage.EventRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PrivateEventServiceImpl implements PrivateEventService {

    private final EventRepository eventRepository;
    private final UserClient userClient;
    private final CategoryRepository categoryRepository;
    private final EventMapper eventMapper;

    @Transactional
    @Override
    public EventResponseDto createEvent(Long userId, EventRequestDto eventRequestDto) {
        log.info("Создание события пользователем: userId={}, title={}", userId, eventRequestDto.getTitle());

        try {
            UserShortDto user = userClient.getById(userId);
            if (user == null || user.getId() == null) {
                throw new NotFoundException("User with id=" + userId + " not found");
            }
        } catch (FeignException e) {
            if (e.status() == 404) {
                throw new NotFoundException("User with id=" + userId + " not found");
            }
            log.error("Feign error: {}", e.getMessage());
            throw new ConditionsNotMetException("Сервис пользователей временно недоступен");
        } catch (Exception e) {
            log.error("Error checking user: {}", e.getMessage());
            throw new ConditionsNotMetException("Сервис пользователей временно недоступен");
        }

        Category category = categoryRepository.findById(eventRequestDto.getCategory())
                .orElseThrow(() -> new NotFoundException("Категория с id=" + eventRequestDto.getCategory() + " не найдена"));

        if (eventRequestDto.getParticipantLimit() != null && eventRequestDto.getParticipantLimit() < 0) {
            throw new ValidationException("Лимит участников не может быть отрицательным");
        }

        if (eventRequestDto.getEventDate().isBefore(LocalDateTime.now().plusHours(2))) {
            throw new ConflictException("Дата события должна быть не ранее чем через 2 часа от текущего момента");
        }

        Event event = Event.builder()
                .annotation(eventRequestDto.getAnnotation())
                .category(category)
                .description(eventRequestDto.getDescription())
                .eventDate(eventRequestDto.getEventDate())
                .initiatorId(userId)
                .location(new EventLocation(
                        eventRequestDto.getLocation().getLat(),
                        eventRequestDto.getLocation().getLon()
                ))
                .paid(eventRequestDto.getPaid() != null ? eventRequestDto.getPaid() : false)
                .participantLimit(eventRequestDto.getParticipantLimit() != null ? eventRequestDto.getParticipantLimit() : 0)
                .requestModeration(eventRequestDto.getRequestModeration() != null ? eventRequestDto.getRequestModeration() : true)
                .state(EventState.PENDING)
                .title(eventRequestDto.getTitle())
                .createdOn(LocalDateTime.now())
                .confirmedRequests(0)
                .views(0L)
                .build();

        Event savedEvent = eventRepository.save(event);
        log.info("Событие создано: id={}, title={}", savedEvent.getId(), savedEvent.getTitle());

        EventResponseDto responseDto = eventMapper.toEventResponseDto(savedEvent);

        UserShortDto initiator = userClient.getById(userId);
        responseDto.setInitiator(initiator);

        return responseDto;
    }

    @Override
    public List<EventShortDto> getEventsByUser(Long userId, int from, int size) {
        log.info("Получение событий пользователя: userId={}, from={}, size={}", userId, from, size);

        validatePaginationParams(from, size);

        Pageable pageable = PageRequest.of(0, size);
        if (from > 0) {
            pageable = PageRequest.of(0, size + from);
        }

        Page<Event> events = eventRepository.findByInitiatorId(userId, pageable);
        List<Event> content = events.getContent();

        if (from > 0 && content.size() > from) {
            content = content.subList(from, content.size());
        }

        return content.stream()
                .limit(size)
                .map(eventMapper::toEventShortDto)
                .collect(Collectors.toList());
    }

    @Override
    public EventResponseDto getEventByUser(Long userId, Long eventId) {
        log.info("Получение события пользователя: userId={}, eventId={}", userId, eventId);

        Event event = eventRepository.findByIdAndInitiatorId(eventId, userId)
                .orElseThrow(() -> new NotFoundException(
                        "Событие с id=" + eventId + " для пользователя с id=" + userId + " не найдено"));

        EventResponseDto responseDto = eventMapper.toEventResponseDto(event);

        UserShortDto initiator = userClient.getById(userId);
        responseDto.setInitiator(initiator);

        return responseDto;
    }

    @Transactional
    @Override
    public EventResponseDto updateEventByUser(Long userId, Long eventId, EventUpdateRequestDto updateRequest) {
        log.info("Обновление события пользователем: userId={}, eventId={}", userId, eventId);

        Event event = eventRepository.findByIdAndInitiatorId(eventId, userId)
                .orElseThrow(() -> new NotFoundException(
                        "Событие с id=" + eventId + " для пользователя с id=" + userId + " не найдено"));

        if (event.getState() == EventState.PUBLISHED) {
            throw new ConflictException("Нельзя редактировать опубликованное событие");
        }

        if (updateRequest.getEventDate() != null &&
                updateRequest.getEventDate().isBefore(LocalDateTime.now().plusHours(2))) {
            throw new ConflictException("Дата события должна быть не ранее чем через 2 часа от текущего момента");
        }

        if (updateRequest.getAnnotation() != null) {
            event.setAnnotation(updateRequest.getAnnotation());
        }
        if (updateRequest.getCategory() != null) {
            Category category = categoryRepository.findById(updateRequest.getCategory())
                    .orElseThrow(() -> new NotFoundException("Категория не найдена"));
            event.setCategory(category);
        }
        if (updateRequest.getDescription() != null) {
            event.setDescription(updateRequest.getDescription());
        }
        if (updateRequest.getEventDate() != null) {
            event.setEventDate(updateRequest.getEventDate());
        }
        if (updateRequest.getLocation() != null) {
            event.setLocation(new EventLocation(
                    updateRequest.getLocation().getLat(),
                    updateRequest.getLocation().getLon()
            ));
        }
        if (updateRequest.getPaid() != null) {
            event.setPaid(updateRequest.getPaid());
        }
        if (updateRequest.getParticipantLimit() != null) {
            event.setParticipantLimit(updateRequest.getParticipantLimit());
        }
        if (updateRequest.getRequestModeration() != null) {
            event.setRequestModeration(updateRequest.getRequestModeration());
        }
        if (updateRequest.getTitle() != null) {
            event.setTitle(updateRequest.getTitle());
        }

        if (updateRequest.getStateAction() != null) {
            switch (updateRequest.getStateAction()) {
                case "SEND_TO_REVIEW":
                    if (event.getState() == EventState.CANCELED) {
                        event.setState(EventState.PENDING);
                    }
                    break;
                case "CANCEL_REVIEW":
                    if (event.getState() == EventState.PENDING) {
                        event.setState(EventState.CANCELED);
                    }
                    break;
                default:
                    log.warn("Unknown state action: {}", updateRequest.getStateAction());
            }
        }

        Event updatedEvent = eventRepository.save(event);
        return eventMapper.toEventResponseDto(updatedEvent);
    }

    private void validatePaginationParams(int from, int size) {
        if (size <= 0) {
            throw new IllegalArgumentException("Size must be positive");
        }
        if (from < 0) {
            throw new IllegalArgumentException("From must be non-negative");
        }
    }
}