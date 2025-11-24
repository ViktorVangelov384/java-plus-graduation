package teamfive.event.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import teamfive.category.model.Category;
import teamfive.category.storage.CategoryRepository;
import teamfive.event.dto.EventRequestDto;
import teamfive.event.dto.EventResponseDto;
import teamfive.event.dto.EventShortDto;
import teamfive.event.dto.EventUpdateRequestDto;
import teamfive.event.mapper.EventMapper;
import teamfive.event.model.Event;
import teamfive.event.model.EventLocation;
import teamfive.event.model.EventState;
import teamfive.event.storage.EventRepository;
import teamfive.exception.ConflictException;
import teamfive.exception.NotFoundException;
import teamfive.user.model.User;
import teamfive.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PrivateEventServiceImpl implements PrivateEventService {

    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final EventMapper eventMapper;

    @Transactional
    @Override
    public EventResponseDto createEvent(Long userId, EventRequestDto eventRequestDto) {
        log.info("Создание события пользователем: userId={}, title={}", userId, eventRequestDto.getTitle());

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id=" + userId + " не найден"));

        Category category = categoryRepository.findById(eventRequestDto.getCategory())
                .orElseThrow(() -> new NotFoundException("Категория с id=" + eventRequestDto.getCategory() + " не найдена"));

        if (eventRequestDto.getEventDate().isBefore(LocalDateTime.now().plusHours(2))) {
            throw new ConflictException("Дата события должна быть не ранее чем через 2 часа от текущего момента");
        }

        Event event = Event.builder()
                .annotation(eventRequestDto.getAnnotation())
                .category(category)
                .description(eventRequestDto.getDescription())
                .eventDate(eventRequestDto.getEventDate())
                .initiator(user)
                .location(new EventLocation(
                        eventRequestDto.getLocation().getLat(),
                        eventRequestDto.getLocation().getLon()
                ))
                .paid(eventRequestDto.getPaid())
                .participantLimit(eventRequestDto.getParticipantLimit())
                .requestModeration(eventRequestDto.getRequestModeration())
                .state(EventState.PENDING)
                .title(eventRequestDto.getTitle())
                .createdOn(LocalDateTime.now())
                .confirmedRequests(0)
                .views(0L)
                .build();

        Event savedEvent = eventRepository.save(event);
        log.info("Событие создано: id={}, title={}", savedEvent.getId(), savedEvent.getTitle());

        return eventMapper.toEventResponseDto(savedEvent);
    }

    @Override
    public List<EventShortDto> getEventsByUser(Long userId, int from, int size) {
        log.info("Получение событий пользователя: userId={}, from={}, size={}", userId, from, size);

        validatePaginationParams(from, size);

        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("Пользователь с id=" + userId + " не найден");
        }

        int page = from / size;
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());

        Page<Event> events = eventRepository.findByInitiatorId(userId, pageable);

        return events.getContent().stream()
                .map(eventMapper::toEventShortDto)
                .collect(Collectors.toList());
    }

    @Override
    public EventResponseDto getEventByUser(Long userId, Long eventId) {
        log.info("Получение события пользователя: userId={}, eventId={}", userId, eventId);

        Event event = eventRepository.findByIdAndInitiatorId(eventId, userId)
                .orElseThrow(() -> new NotFoundException(
                        "Событие с id=" + eventId + " для пользователя с id=" + userId + " не найдено"));

        return eventMapper.toEventResponseDto(event);
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

        if ("SEND_TO_REVIEW".equals(updateRequest.getStateAction())) {
            event.setState(EventState.PENDING);
        } else if ("CANCEL_REVIEW".equals(updateRequest.getStateAction())) {
            event.setState(EventState.CANCELED);
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