package ru.practicum.event.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.category.model.Category;
import ru.practicum.category.storage.CategoryRepository;
import ru.practicum.client.RequestClient;
import ru.practicum.client.UserClient;
import ru.practicum.dto.event.EventForRequestDto;
import ru.practicum.dto.user.UserShortDto;
import ru.practicum.enums.EventState;
import ru.practicum.event.dto.EventResponseDto;
import ru.practicum.dto.event.EventShortDto;
import ru.practicum.event.dto.EventUpdateRequestDto;
import ru.practicum.event.exception.ConflictException;
import ru.practicum.event.exception.NotFoundException;
import ru.practicum.event.exception.ValidationException;
import ru.practicum.event.mapper.EventMapper;
import ru.practicum.event.model.Event;
import ru.practicum.event.model.EventLocation;
import ru.practicum.event.storage.EventRepository;
import ru.yandex.practicum.client.StatsClient;
import ru.yandex.practicum.dto.StatsResponseDto;

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
    private final StatsClient statsClient;
    private final RequestClient requestClient;
    private final UserClient userClient;

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_DATE_TIME;

    @Override
    public List<EventResponseDto> getEventsByAdmin(List<Long> users, List<String> states,
                                                   List<Long> categories, String rangeStart,
                                                   String rangeEnd, int from, int size) {
        log.info("Поиск событий администратором: users={}, states={}, categories={}", users, states, categories);

        if (categories != null && !categories.isEmpty()) {
            for (Long categoryId : categories) {
                if (categoryId == null || categoryId <= 0) {
                    throw new ValidationException("ID категории должен быть положительным числом");
                }
                if (!categoryRepository.existsById(categoryId)) {
                    throw new ValidationException("Категория с id=" + categoryId + " не существует");
                }
            }
        }

        validatePaginationParams(from, size);
        int page = calculatePageNumber(from, size);
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").ascending());

        Specification<Event> spec = Specification.where(null);

        if (users != null && !users.isEmpty()) {
            spec = spec.and((root, query, cb) ->
                    root.get("initiatorId").in(users));
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

        events = applyConfirmedRequestsToEvents(events);

        events = applyViewsToEvents(events);

        Set<Long> userIds = events.stream()
                .map(Event::getInitiatorId)
                .collect(Collectors.toSet());

        Map<Long, UserShortDto> usersMap = new HashMap<>();
        if (!userIds.isEmpty()) {
            try {
                usersMap = userClient.getAllUsersByIds(new ArrayList<>(userIds));
            } catch (Exception e) {
                log.warn("Failed to fetch users: {}", e.getMessage());
            }
        }

        final Map<Long, UserShortDto> finalUsersMap = usersMap;
        return events.stream()
                .map(event -> {
                    EventResponseDto dto = eventMapper.toEventResponseDto(event);
                    UserShortDto initiator = finalUsersMap.get(event.getInitiatorId());
                    if (initiator != null) {
                        dto.setInitiator(initiator);
                    } else {
                        UserShortDto stub = new UserShortDto();
                        stub.setId(event.getInitiatorId());
                        dto.setInitiator(stub);
                    }
                    return dto;
                })
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

        if (categories != null && !categories.isEmpty()) {
            for (Long categoryId : categories) {
                if (categoryId == null || categoryId <= 0) {
                    throw new ValidationException("ID категории должен быть положительным числом");
                }
                if (!categoryRepository.existsById(categoryId)) {
                    throw new ValidationException("Категория с id=" + categoryId + " не существует");
                }
            }
        }

        validatePaginationParams(from, size);
        Pageable pageable = createPageable(sort, from, size);

        Specification<Event> spec = Specification.where((root, query, cb) ->
                cb.equal(root.get("state"), EventState.PUBLISHED));

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

        Page<Event> eventsPage = eventRepository.findAll(spec, pageable);
        List<Event> events = eventsPage.getContent();

        events = applyConfirmedRequestsToEvents(events);

        events = applyViewsToEvents(events);

        if (onlyAvailable != null && onlyAvailable) {
            events = events.stream()
                    .filter(event -> event.getConfirmedRequests() < event.getParticipantLimit())
                    .collect(Collectors.toList());
        }
        Set<Long> userIds = events.stream()
                .map(Event::getInitiatorId)
                .collect(Collectors.toSet());

        Map<Long, UserShortDto> usersMap = new HashMap<>();
        if (!userIds.isEmpty()) {
            try {
                usersMap = userClient.getAllUsersByIds(new ArrayList<>(userIds));
            } catch (Exception e) {
                log.warn("Failed to fetch users: {}", e.getMessage());
            }
        }

        final Map<Long, UserShortDto> finalUsersMap = usersMap;
        return events.stream()
                .map(event -> {
                    EventShortDto dto = eventMapper.toEventShortDto(event);
                    UserShortDto initiator = finalUsersMap.get(event.getInitiatorId());
                    if (initiator != null) {
                        dto.setInitiator(initiator);
                    } else {
                        UserShortDto stub = new UserShortDto();
                        stub.setId(event.getInitiatorId());
                        dto.setInitiator(stub);
                    }
                    return dto;
                })
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

        int confirmedCount = requestClient.getCountConfirmedRequestsByEventId(id);
        event.setConfirmedRequests(confirmedCount);

        Long views = getViewsClient(event);
        event.setViews(views);

        eventRepository.save(event);

        EventResponseDto responseDto = eventMapper.toEventResponseDto(event);
        responseDto.setConfirmedRequests(confirmedCount);
        responseDto.setViews(views);

        return responseDto;
    }

    @Override
    public EventForRequestDto getEventForRequest(Long eventId) {
        log.info("Getting event for request: eventId={}", eventId);
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Event not found: " + eventId));

        EventForRequestDto dto = new EventForRequestDto();
        dto.setId(event.getId());
        dto.setState(event.getState());
        dto.setInitiatorId(event.getInitiatorId());
        dto.setParticipantLimit(event.getParticipantLimit());
        dto.setRequestModeration(event.getRequestModeration());
        return dto;
    }

    @Override
    public boolean isUserInitiator(Long userId, Long eventId) {
        log.info("Checking if user {} is initiator of event {}", userId, eventId);
        return eventRepository.existsByIdAndInitiatorId(eventId, userId);
    }

    @Override
    @Transactional
    public void updateConfirmedRequestsCounts(Map<Long, Integer> updates) {
        log.info("Updating confirmed requests counts: {}", updates);
        for (Map.Entry<Long, Integer> entry : updates.entrySet()) {
            eventRepository.updateConfirmedRequestsCount(entry.getKey(), entry.getValue());
        }
    }

    @Override
    public EventResponseDto getEventByIdForInternalUse(Long id) {
        log.info("Получение события по id для внутреннего использования: {}", id);

        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Событие с id=" + id + " не найдено"));

        int confirmedCount = requestClient.getCountConfirmedRequestsByEventId(id);

        EventResponseDto responseDto = eventMapper.toEventResponseDto(event);
        responseDto.setConfirmedRequests(confirmedCount);

        return responseDto;
    }

    private List<Event> applyConfirmedRequestsToEvents(List<Event> events) {
        List<Long> eventsIds = events.stream().map(Event::getId).toList();
        Map<Long, Integer> requestsByEventIds = requestClient.getCountConfirmedRequestsByEventIds(eventsIds);

        return events.stream().peek(event ->
                event.setConfirmedRequests(requestsByEventIds.getOrDefault(event.getId(), 0))
        ).collect(Collectors.toList());
    }

    private List<Event> applyViewsToEvents(List<Event> events) {
        return events.stream().peek(event -> {
            Long views = getViewsClient(event);
            event.setViews(views);
        }).collect(Collectors.toList());
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
            throw new IllegalArgumentException("Size must be positive");
        }
        if (from < 0) {
            throw new IllegalArgumentException("From must be non-negative");
        }
    }

    private int calculatePageNumber(int from, int size) {
        return from / size;
    }

    private Long getViewsClient(Event event) {
        try {
            String uri = "/events/" + event.getId();
            LocalDateTime startDate = event.getPublishedOn() != null
                    ? event.getPublishedOn()
                    : LocalDateTime.now().minusMonths(1);

            List<StatsResponseDto> stats = statsClient.getStats(
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