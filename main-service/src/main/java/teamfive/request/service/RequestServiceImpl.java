package teamfive.request.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import teamfive.event.dto.EventRequestStatusUpdateRequest;
import teamfive.event.dto.EventResponseDto;
import teamfive.event.model.Event;
import teamfive.event.model.EventState;
import teamfive.event.service.EventService;
import teamfive.event.storage.EventRepository;
import teamfive.exception.ConflictException;
import teamfive.exception.DuplicatedException;
import teamfive.exception.NotFoundException;
import teamfive.request.dto.ParticipationRequestDto;
import teamfive.request.enums.RequestStatus;
import teamfive.request.mapper.RequestMapper;
import teamfive.request.model.ParticipationRequest;
import teamfive.request.repository.RequestRepository;
import teamfive.user.dto.UserDto;
import teamfive.user.service.UserService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@AllArgsConstructor
@Transactional(readOnly = true)
public class RequestServiceImpl implements RequestService {

    private final RequestRepository repository;
    private final UserService userService;
    private final RequestMapper mapper;
    private final EventService eventService;
    private final EventRepository eventRepository;


    @Override
    @Transactional
    public ParticipationRequestDto create(Long userId, Long eventId, EventResponseDto event) {
        log.info("Создание запроса: userId={}, eventId={}, eventState={}, participantLimit={}",
                userId, eventId, event.getState(), event.getParticipantLimit());

        if (repository.findByEventIdAndRequesterId(eventId, userId).isPresent())
            throw new DuplicatedException("Такая заявка уже создана");

        if (event.getInitiator().getId().equals(userId))
            throw new ConflictException("Инициатор события не может добавить запрос на участие в своём событии");

        if (!EventState.PUBLISHED.toString().equals(event.getState())) {
            throw new ConflictException("Нельзя участвовать в неопубликованном событии");
        }

        int confirmedCount = repository.findAllByEventIdAndStatus(eventId, RequestStatus.CONFIRMED.toString()).size();


        if (event.getParticipantLimit() > 0 && confirmedCount >= event.getParticipantLimit()) {
            throw new ConflictException("Достигнут лимит участников");
        }
        RequestStatus status = RequestStatus.PENDING;

        /*boolean isUnlimitedEvent = event.getParticipantLimit() == 0;
        boolean isModerationDisabled = event.getRequestModeration() == null || !event.getRequestModeration();
        boolean hasAvailableSlots = confirmedCount < event.getParticipantLimit();

        log.info("Условия: isUnlimitedEvent={}, isModerationDisabled={}, hasAvailableSlots={}",
                isUnlimitedEvent, isModerationDisabled, hasAvailableSlots);

        if (isUnlimitedEvent) {
            status = RequestStatus.CONFIRMED;
            log.info("Событие БЕЗ лимита участников - статус CONFIRMED");
        } else if (isModerationDisabled && hasAvailableSlots) {
            status = RequestStatus.CONFIRMED;
            log.info("Модерация ОТКЛЮЧЕНА и есть места - статус CONFIRMED");
        } else {
            status = RequestStatus.PENDING;
            log.info("Требуется модерация или нет мест - статус PENDING");
        }*/

        if (event.getParticipantLimit() == 0) {
            status = RequestStatus.CONFIRMED;
            log.info("Событие БЕЗ лимита участников - статус CONFIRMED");
        } else {
            if (confirmedCount >= event.getParticipantLimit()) {
                throw new ConflictException("Достигнут лимит участников");
            }

            if (event.getRequestModeration() == null || !event.getRequestModeration()) {
                status = RequestStatus.CONFIRMED;
                log.info("Модерация отключена - статус CONFIRMED");
            } else {
                status = RequestStatus.PENDING;
                log.info("Требуется модерация - статус PENDING");
            }
        }

        UserDto user = userService.get(userId);


        ParticipationRequest request = ParticipationRequest.builder()
                .requesterId(user.getId())
                .eventId(event.getId())
                .status(status.toString())
                .created(LocalDateTime.now())
                .build();

        ParticipationRequest participationRequest = repository.save(request);
        repository.flush();
        log.info("Запрос успешно создан. Параметры: {}", participationRequest);
        return mapper.toDto(participationRequest);
    }

    @Override
    public List<ParticipationRequestDto> getRequests(Long userId) {
        UserDto user = userService.get(userId);
        return repository.findAllByRequesterId(user.getId()).stream().map(mapper::toDto).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public ParticipationRequestDto cancelRequest(Long userId, Long requestId) {
        ParticipationRequest request = repository.findById(requestId).orElseThrow(() -> new NotFoundException("Заявка не найдена"));

        UserDto user = userService.get(request.getRequesterId());
        if (!user.getId().equals(userId)) {
            log.error("Попытка отменить чужую заявку: userId={}, заявка принадлежит userId={}", userId, user.getId());
            throw new ConflictException("Пользователь, который не является автором заявки, не может её отменить.");
        }

        request.setStatus(RequestStatus.CANCELED.toString());
        log.info("Статус заявки с id={} изменен на CANCELED", requestId);

        ParticipationRequestDto requestDto = mapper.toDto(repository.save(request));
        repository.flush();
        log.info("Участие в событии для пользователя с id={} отменено", userId);

        return requestDto;
    }

    @Override
    public List<ParticipationRequestDto> getRequestsForUserEvent(Long userId, Long eventId) {
        log.info("Получение запросов на участие для события: userId={}, eventId={}", userId, eventId);

        List<ParticipationRequest> requests = repository.findAllByEventId(eventId);
        log.debug("Найдено запросов для события {}: {}", eventId, requests.size());

        return requests.stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public List<ParticipationRequestDto> updateRequestStatuses(Long userId, Long eventId,
                                                               EventRequestStatusUpdateRequest updateRequest) {
        log.info("Обновление статусов запросов: userId={}, eventId={}, requestIds={}, status={}",
                userId, eventId, updateRequest.getRequestIds(), updateRequest.getStatus());

        EventResponseDto event = eventService.getEventByIdForInternalUse(eventId);

        if (!event.getInitiator().getId().equals(userId)) {
            throw new ConflictException("Только инициатор события может обновлять запросы на участие");
        }

        List<ParticipationRequest> confirmedRequests = repository.findAllByEventIdAndStatus(eventId, RequestStatus.CONFIRMED.toString());
        int currentConfirmed = confirmedRequests.size();
        int participantLimit = event.getParticipantLimit() != null ? event.getParticipantLimit() : 0;

        if (RequestStatus.CONFIRMED.toString().equals(updateRequest.getStatus()) && participantLimit > 0) {
            int requestsToConfirm = updateRequest.getRequestIds().size();

            if (currentConfirmed + requestsToConfirm > participantLimit) {
                throw new ConflictException("Достигнут лимит участников события");
            }
        }

        List<ParticipationRequest> requestsToUpdate = repository.findAllByIdIn(updateRequest.getRequestIds());

        if (requestsToUpdate.size() != updateRequest.getRequestIds().size()) {
            throw new NotFoundException("Некоторые запросы не найдены");
        }

        int newlyConfirmed = 0;
        int newlyRejected = 0;

        for (ParticipationRequest request : requestsToUpdate) {
            if (!request.getEventId().equals(eventId)) {
                throw new ConflictException("Запрос не принадлежит указанному событию");
            }

            if (!RequestStatus.PENDING.toString().equals(request.getStatus())) {
                throw new ConflictException("Можно изменять только запросы в статусе PENDING");
            }

            if (RequestStatus.CONFIRMED.toString().equals(updateRequest.getStatus())) {
                newlyConfirmed++;
            } else if (RequestStatus.REJECTED.toString().equals(updateRequest.getStatus())) {
                newlyRejected++;
            }

            request.setStatus(updateRequest.getStatus());
        }

        List<ParticipationRequest> updatedRequests = repository.saveAll(requestsToUpdate);
        repository.flush();

        if (newlyConfirmed > 0 || newlyRejected > 0) {
            updateEventConfirmedRequests(eventId, newlyConfirmed, newlyRejected);
        }

        log.info("Статусы запросов успешно обновлены");

        return updatedRequests.stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }

    private void updateEventConfirmedRequests(Long eventId, int newlyConfirmed, int newlyRejected) {

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Событие с id=" + eventId + " не найдено"));

        int currentConfirmed = event.getConfirmedRequests() != null ? event.getConfirmedRequests() : 0;
        event.setConfirmedRequests(currentConfirmed + newlyConfirmed - newlyRejected);

        eventRepository.save(event);
        log.info("Обновлен счетчик подтвержденных запросов для события {}: {}", eventId, event.getConfirmedRequests());
    }
}

