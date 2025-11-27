package teamfive.event.service;

import dto.StatDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import teamfive.category.model.Category;
import teamfive.category.storage.CategoryRepository;
import teamfive.client.ParamRequest;
import teamfive.client.StatClient;
import teamfive.event.dto.EventResponseDto;
import teamfive.event.dto.EventShortDto;
import teamfive.event.dto.EventUpdateRequestDto;
import teamfive.event.mapper.EventMapper;
import teamfive.event.model.Event;
import teamfive.event.model.EventState;
import teamfive.event.model.EventLocation;
import teamfive.event.storage.EventRepository;
import teamfive.exception.ConflictException;
import teamfive.exception.NotFoundException;
import teamfive.exception.ValidationException;
import teamfive.request.model.ParticipationRequest;
import teamfive.request.repository.RequestRepository;


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
    private final StatClient client;

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

        Page<Event> events = eventRepository.findAll(spec, pageable);

        return events.getContent().stream()
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

        Page<Event> events = eventRepository.findAll(spec, pageable);

        return events.getContent().stream()
                .map(event -> {
                    Long views = getViewsClientForList(event);
                    event.setViews(views);
                    return eventMapper.toEventShortDto(event);
                })
                .collect(Collectors.toList());
    }

    @Override
    public EventResponseDto getEventById(Long id) {
        log.info("Получение события по id: {}", id);

        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Событие с id=" + id + " не найдено"));

        if (event.getState() != EventState.PUBLISHED) {
            throw new NotFoundException("Событие с id=" + id + " не найдено");
        }

        Long viewsFromStats = getViewsClient(event);
        log.info("Просмотры из статистики: {}", viewsFromStats);

        event.setViews(viewsFromStats);

        List<ParticipationRequest> requests = requestRepository.findAllByEventId(id);
        int confirmedCount = (int) requests.stream()
                .filter(request -> "CONFIRMED".equals(request.getStatus()))
                .count();

        EventResponseDto responseDto = eventMapper.toEventResponseDto(event);
        responseDto.setConfirmedRequests(confirmedCount);

        return responseDto;
    }

    private Long getViewsClient(Event event) {
        try {
            String uri = "/events/" + event.getId();
            LocalDateTime publishDate = event.getPublishedOn() != null ?
                    event.getPublishedOn() : LocalDateTime.now().minusYears(1);

            log.info("URI для статистики: {}, publishDate: {}", uri, publishDate);

            ParamRequest paramRequest = new ParamRequest(
                    publishDate,
                    LocalDateTime.now(),
                    Collections.singletonList(uri),
                    true);

            List<StatDto> stats = client.getStats(paramRequest);

            log.info("Получено {} записей статистики", stats.size());

            Long totalViews = stats.stream()
                    .mapToLong(StatDto::getHits)
                    .sum();

            log.info("Общее количество просмотров из статистики: {}", totalViews);
            return totalViews;

        } catch (Exception e) {
            log.error("Ошибка при получении статистики: {}", e.getMessage());
            return 0L;
        }
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

            ParamRequest paramRequest = new ParamRequest(
                    publishDate,
                    LocalDateTime.now(),
                    Collections.singletonList(uri),
                    false);

            List<StatDto> stats = client.getStats(paramRequest);

            Long totalViews = stats.stream()
                    .mapToLong(StatDto::getHits)
                    .sum();

            log.info("Просмотры для списка событий {}: {}", event.getId(), totalViews);
            return totalViews;

        } catch (Exception e) {
            log.error("Ошибка при получении статистики для списка: {}", e.getMessage());
            return event.getViews() != null ? event.getViews() : 0L;
        }
    }
}
