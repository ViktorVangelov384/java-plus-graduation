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
import teamfive.user.repository.UserRepository;

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
    // наверное надо убрать !
    private final UserRepository userRepository;
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

        List<Long> eventIds = events.getContent().stream()
                .map(Event::getId)
                .collect(Collectors.toList());
        eventRepository.incrementViewsBatch(eventIds);

        List<Event> updatedEvents = eventRepository.findByIdIn(eventIds);

        return updatedEvents.stream()
                .map(eventMapper::toEventShortDto)
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
        eventRepository.save(event);

        // Тоже костыль)))))
        List<Event> list = new ArrayList<>();
        list.add(event);
        LocalDateTime now = LocalDateTime.now();
        log.info("LocalDateTime now = {} ", now);

        LocalDateTime inTwoHours = now.plusHours(2);
        log.info("LocalDateTime inTwoHours = {} ", inTwoHours);
        event.setViews(getViewsClient(list, now, inTwoHours));
        log.info("Проверка получения просмотров VIEWS= {} ", getViewsClient(list, now, inTwoHours));


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

    private Long getViewsClient(List<Event> events, LocalDateTime start, LocalDateTime end) {
        Map<Long, String> eventIds = events
                .stream()
                .collect(Collectors.toMap(
                        Event::getId,
                        event -> "/events/" + event.getId()
                ));

        // ЖЖЖЖЕСТКИЙ костыльль. Пардонте делал наскоряк. Это потому что в клиенте стринги почему то!!!
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
        String startForClient = formatter.format(start);
        String endForClient = formatter.format(end);


        Optional<Collection<StatDto>> statDtos = Optional.ofNullable(client.getStats(
                startForClient,
                endForClient,
                eventIds.values().stream().toList(),
                true
        ));

        if (statDtos.isPresent() && !statDtos.get().isEmpty()) {
            Map<String, Long> hitsStats = statDtos.get()
                    .stream()
                    .collect(Collectors.toMap(StatDto::getUri, StatDto::getHits));

            long totalViews = statDtos.get()
                    .stream()
                    .mapToLong(StatDto::getHits)
                    .sum();

            events.forEach(event -> {
                Long hit = hitsStats.get(eventIds.get(event.getId()));
                event.setViews(Objects.isNull(hit) ? 0L : hit);
            });

            return totalViews;
        } else {
            events.forEach(event -> event.setViews(0L));
            return 0L;
        }
    }





    @Override
    public EventResponseDto getEventByIdForInternalUse(Long id) {
        log.info("Получение события по id для внутреннего использования: {}", id);

        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Событие с id=" + id + " не найдено"));

        eventRepository.incrementViews(id);
        event.setViews(event.getViews() + 1);

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
            throw new IllegalArgumentException("Size must be positive");
        }
        if (from < 0) {
            throw new IllegalArgumentException("From must be non-negative");
        }
    }

    private int calculatePageNumber(int from, int size) {
        return from / size;
    }
}
