package ru.yandex.practicum.event.service;

import ru.yandex.practicum.client.StatsClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.category.model.Category;
import ru.yandex.practicum.category.storage.CategoryRepository;
import ru.yandex.practicum.dto.StatsResponseDto;
import ru.yandex.practicum.event.dto.EventResponseDto;
import ru.yandex.practicum.event.dto.EventShortDto;
import ru.yandex.practicum.event.dto.EventUpdateRequestDto;
import ru.yandex.practicum.event.mapper.EventMapper;
import ru.yandex.practicum.event.model.Event;
import ru.yandex.practicum.event.model.EventState;
import ru.yandex.practicum.event.model.EventLocation;
import ru.yandex.practicum.event.storage.EventRepository;
import ru.yandex.practicum.exception.ConflictException;
import ru.yandex.practicum.exception.NotFoundException;
import ru.yandex.practicum.exception.ValidationException;
import ru.yandex.practicum.request.model.ParticipationRequest;
import ru.yandex.practicum.request.repository.RequestRepository;


import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EventServiceImpl implements EventService {

    private final EventRepository eventRepository;
    private final CategoryRepository categoryRepository;
    private final EventMapper eventMapper;
    private final RequestRepository requestRepository;
    private final StatsClient client;

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_DATE_TIME;

    @Override
    public List<EventResponseDto> getEventsByAdmin(List<Long> users, List<String> states,
                                                   List<Long> categories, String rangeStart,
                                                   String rangeEnd, int from, int size) {
        log.info("Поиск событий администратором: users={}, states={}, categories={}", users, states, categories);

        validatePaginationParams(from, size);
        int page = calculatePageNumber(from, size);
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").ascending());

        Specification<Event> spec = Specification.where(null);

        if (users != null && !users.isEmpty()) {
            spec = spec.and((root, query, cb) ->
                    root.get("initiator").get("id").in(users));
        }

        if (states != null && !states.isEmpty()) {
            spec = spec.and((root, query, cb) ->
                    root.get("state").as(String.class).in(states));
        }

        if (categories != null && !categories.isEmpty()) {
            spec = spec.and((root, query, cb) ->
                    root.get("category").get("id").in(categories));
        }

        if (rangeStart != null) {
            LocalDateTime start = LocalDateTime.parse(rangeStart, FORMATTER);
            spec = spec.and((root, query, cb) ->
                    cb.greaterThanOrEqualTo(root.get("eventDate"), start));
        }

        if (rangeEnd != null) {
            LocalDateTime end = LocalDateTime.parse(rangeEnd, FORMATTER);
            spec = spec.and((root, query, cb) ->
                    cb.lessThanOrEqualTo(root.get("eventDate"), end));
        }

        Page<Event> eventsPage = eventRepository.findAll(spec, pageable);
        List<Event> events = eventsPage.getContent();

        for (Event event : events) {
            List<ParticipationRequest> requests = requestRepository.findAllByEventId(event.getId());
            int confirmedCount = (int) requests.stream()
                    .filter(r -> "CONFIRMED".equals(r.getStatus()))
                    .count();
            event.setConfirmedRequests(confirmedCount);

            Long views = getViewsClientForList(event);
            event.setViews(views);
        }

        return events.stream()
                .map(eventMapper::toEventResponseDto)
                .collect(Collectors.toList());
    }

    @Transactional
    @Override
    public EventResponseDto updateEventByAdmin(Long eventId, EventUpdateRequestDto updateRequest) {
        log.info("Обновление события администратором: eventId={}", eventId);

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Событие с id=" + eventId + " не найдено"));


        if ("PUBLISH_EVENT".equals(updateRequest.getStateAction())) {
            if (event.getState() != EventState.PENDING) {
                throw new ConflictException("Событие можно публиковать только в состоянии PENDING");
            }
            if (event.getEventDate().isBefore(LocalDateTime.now().plusHours(1))) {
                throw new ConflictException("Дата начала события должна быть не ранее чем за час от публикации");
            }
            event.setState(EventState.PUBLISHED);
            event.setPublishedOn(LocalDateTime.now());
        }

        if ("REJECT_EVENT".equals(updateRequest.getStateAction())) {
            if (event.getState() == EventState.PUBLISHED) {
                throw new ConflictException("Нельзя отклонить опубликованное событие");
            }
            event.setState(EventState.CANCELED);
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

        Event updatedEvent = eventRepository.save(event);
        return eventMapper.toEventResponseDto(updatedEvent);
    }

    @Override
    public List<EventShortDto> getEventsByPublic(String text, List<Long> categories,
                                                 Boolean paid, String rangeStart,
                                                 String rangeEnd, Boolean onlyAvailable,
                                                 String sort, int from, int size) {
        log.info("Поиск событий публичный: text={}, categories={}, paid={}", text, categories, paid);


        validatePaginationParams(from, size);

        Pageable pageable = createPageable(sort, from, size);

        Specification<Event> spec = Specification.where((root, query, cb) ->
                cb.equal(root.get("state"), EventState.PUBLISHED));


        if (categories != null && !categories.isEmpty()) {
            boolean anyCategoryExists = false;
            for (Long categoryId : categories) {
                if (categoryRepository.existsById(categoryId)) {
                    anyCategoryExists = true;
                    break;
                }
            }
            if (!anyCategoryExists) {
                throw new ValidationException("Указанные категории не существуют");
            }
        }

        if (categories != null && !categories.isEmpty()) {
            spec = spec.and((root, query, cb) ->
                    root.get("category").get("id").in(categories));
        }

        if (paid != null) {
            spec = spec.and((root, query, cb) ->
                    cb.equal(root.get("paid"), paid));
        }

        LocalDateTime startDateTime = rangeStart != null ?
                LocalDateTime.parse(rangeStart.replace(' ', 'T'), FORMATTER) : LocalDateTime.now();
        spec = spec.and((root, query, cb) ->
                cb.greaterThan(root.get("eventDate"), startDateTime));

        if (rangeEnd != null) {
            LocalDateTime endDateTime = LocalDateTime.parse(rangeEnd.replace(' ', 'T'), FORMATTER);
            spec = spec.and((root, query, cb) ->
                    cb.lessThan(root.get("eventDate"), endDateTime));
        }

        if (onlyAvailable != null && onlyAvailable) {
            spec = spec.and((root, query, cb) ->
                    cb.or(
                            cb.isNull(root.get("participantLimit")),
                            cb.equal(root.get("participantLimit"), 0),
                            cb.lessThan(root.get("confirmedRequests"), root.get("participantLimit"))
                    ));
        }

        Page<Event> eventsPage = eventRepository.findAll(spec, pageable);
        List<Event> events = eventsPage.getContent();

        for (Event event : events) {
            List<ParticipationRequest> requests = requestRepository.findAllByEventId(event.getId());
            int confirmedCount = (int) requests.stream()
                    .filter(r -> "CONFIRMED".equals(r.getStatus()))
                    .count();
            event.setConfirmedRequests(confirmedCount);

            Long views = getViewsClientForList(event);
            event.setViews(views);
        }

        return events.stream()
                .map(eventMapper::toEventShortDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public EventResponseDto getEventById(Long id) {
        log.info("Получение события по id: {}", id);

        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Событие с id=" + id + " не найдено"));

        if (event.getState() != EventState.PUBLISHED) {
            throw new NotFoundException("Событие с id=" + id + " не найдено");
        }

        if (event.getPublishedOn() == null) {
            log.warn("Событие {} опубликовано, но publishedOn is null", id);
            event.setPublishedOn(LocalDateTime.now().minusDays(1));
        }

        Long views = getViewsClient(event);
        event.setViews(views);

        List<ParticipationRequest> requests = requestRepository.findAllByEventId(id);
        int confirmedCount = (int) requests.stream()
                .filter(request -> "CONFIRMED".equals(request.getStatus()))
                .count();

        EventResponseDto responseDto = eventMapper.toEventResponseDto(event);
        responseDto.setConfirmedRequests(confirmedCount);
        responseDto.setViews(views);

        return responseDto;
    }

    @Override
    public EventResponseDto getEventByIdForInternalUse(Long id) {
        log.info("Получение события по id для внутреннего использования: {}", id);

        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Событие с id=" + id + " не найдено"));

        List<ParticipationRequest> requests = requestRepository.findAllByEventId(id);
        int confirmedCount = 0;
        for (ParticipationRequest request : requests) {
            if ("CONFIRMED".equals(request.getStatus())) {
                confirmedCount++;
            }
        }

        EventResponseDto responseDto = eventMapper.toEventResponseDto(event);
        responseDto.setConfirmedRequests(confirmedCount);

        return responseDto;
    }

    private Pageable createPageable(String sort, int from, int size) {
        validatePaginationParams(from, size);

        Sort sorting = Sort.unsorted();

        if ("EVENT_DATE".equals(sort)) {
            sorting = Sort.by("eventDate").descending();
        } else if ("VIEWS".equals(sort)) {
            sorting = Sort.by("views").descending();
        }

        int page = calculatePageNumber(from, size);
        return PageRequest.of(page, size, sorting);
    }

    private void validatePaginationParams(int from, int size) {
        if (size <= 0) {
            throw new java.lang.IllegalArgumentException("Size must be positive");
        }
        if (from < 0) {
            throw new java.lang.IllegalArgumentException("From must be non-negative");
        }
    }

    private int calculatePageNumber(int from, int size) {
        return from / size;
    }

    private Long getViewsClientForList(Event event) {
        try {
            String uri = "/events/" + event.getId();
            LocalDateTime publishDate = event.getPublishedOn() != null ?
                    event.getPublishedOn() : LocalDateTime.now().minusYears(1);

            List<StatsResponseDto> stats = client.getStats(
                    publishDate,
                    LocalDateTime.now(),
                    Collections.singletonList(uri),
                    true
            );

            Long totalViews = stats.stream().mapToLong(StatsResponseDto::getHits).sum();

            log.info("Просмотры для списка событий {}: {}", event.getId(), totalViews);
            return totalViews;

        } catch (Exception e) {
            log.error("Ошибка при получении статистики для списка: {}", e.getMessage());
            return event.getViews() != null ? event.getViews() : 0L;
        }
    }

    private Long getViewsClient(Event event) {
        try {
            String uri = "/events/" + event.getId();
            LocalDateTime startDate = event.getPublishedOn() != null
                    ? event.getPublishedOn()
                    : LocalDateTime.now().minusMonths(1);

            List<StatsResponseDto> stats = client.getStats(
                    startDate,
                    LocalDateTime.now(),
                    Collections.singletonList(uri),
                    true
            );

            Long views = stats.stream().mapToLong(StatsResponseDto::getHits).sum();
            log.info("Получены просмотры для события {}: {}", event.getId(), views);
            return views;

        } catch (Exception e) {
            log.error("Ошибка при получении просмотров для события {}: {}", event.getId(), e.getMessage());
            return 0L;
        }
    }
}
