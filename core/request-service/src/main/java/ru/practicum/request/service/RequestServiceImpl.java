package ru.practicum.request.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.client.EventClient;
import ru.practicum.client.UserClient;
import ru.practicum.dto.event.EventForRequestDto;
import ru.practicum.enums.EventState;
import ru.practicum.enums.RequestStatus;
import ru.practicum.dto.event.EventRequestStatusUpdateRequest;
import ru.practicum.request.dto.EventRequestStatusUpdateResult;
import ru.practicum.request.dto.ParticipationRequestDto;
import ru.practicum.request.exception.ConditionsNotMetException;
import ru.practicum.request.exception.NotFoundException;
import ru.practicum.request.mapper.RequestMapper;
import ru.practicum.request.model.Request;
import ru.practicum.request.repository.RequestRepository;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static ru.practicum.enums.RequestStatus.*;

@Slf4j
@Service
@AllArgsConstructor
@Transactional(readOnly = true)
public class RequestServiceImpl implements RequestService {

    private final RequestRepository requestRepository;
    private final UserClient userClient;
    private final EventClient eventClient;
    private final RequestMapper requestMapper;

    @Override
    @Transactional
    public ParticipationRequestDto create(Long userId, Long eventId) {

        try {
            userClient.checkUserExists(userId);
        } catch (Exception e) {
            log.error("User service unavailable or user not found: {}", e.getMessage());
            throw new ConditionsNotMetException("Сервис пользователей временно недоступен");
        }

        if (requestRepository.existsByEventIdAndRequesterId(eventId, userId)) {
            throw new ConditionsNotMetException("Нельзя добавить повторный запрос на участие в событии");
        }

        try {
            boolean isInitiator = eventClient.checkUserIsInitiator(userId, eventId);
            if (isInitiator) {
                throw new ConditionsNotMetException("Инициатор события не может создать запрос на участие в своём событии");
            }
        } catch (Exception e) {
            log.error("Failed to check if user is initiator: {}", e.getMessage());
            throw new ConditionsNotMetException("Сервис событий временно недоступен");
        }

        EventForRequestDto event;
        try {
            event = eventClient.getById(eventId);
            if (event == null) {
                throw new NotFoundException("Событие не найдено");
            }
            if (event.getState() != EventState.PUBLISHED) {
                throw new ConditionsNotMetException("Нельзя участвовать в неопубликованном событии");
            }
        } catch (Exception e) {
            log.error("Event service unavailable or event not found: {}", e.getMessage());
            throw new ConditionsNotMetException("Сервис событий временно недоступен");
        }

        int currentConfirmed = getConfirmedRequestsCount(eventId);
        checkParticipantLimit(event.getParticipantLimit(), currentConfirmed);

        Request savedRequest = saveRequest(event, userId);

        updateEventConfirmedRequestsCount(eventId);

        return requestMapper.toDto(savedRequest);
    }

    @Override
    public List<ParticipationRequestDto> getRequests(Long userId) {
        List<Request> requests = requestRepository.findAllByRequesterId(userId);
        return requestMapper.toDtoList(requests);
    }

    @Override
    public List<ParticipationRequestDto> getRequestsForUserEvent(Long userId, Long eventId) {

        EventForRequestDto event = eventClient.getById(eventId);
        if (event == null) {
            throw new NotFoundException("Событие не найдено");
        }

        if (!eventClient.checkUserIsInitiator(userId, eventId)) {
            throw new ConditionsNotMetException("Пользователь не является владельцем события");
        }

        List<Request> requests = requestRepository.findAllByEventId(eventId);
        return requestMapper.toDtoList(requests);
    }

    @Override
    @Transactional
    public EventRequestStatusUpdateResult updateRequestStatus(Long userId, Long eventId,
                                                              EventRequestStatusUpdateRequest updateRequest) {

        EventForRequestDto event = eventClient.getById(eventId);
        if (event == null) {
            throw new NotFoundException("Событие не найдено");
        }

        if (event.getState() != EventState.PUBLISHED) {
            throw new ConditionsNotMetException("Нельзя изменять статус заявок на неопубликованное событие");
        }

        if (!event.getInitiatorId().equals(userId)) {
            throw new ConditionsNotMetException("Пользователь не является владельцем события");
        }

        List<Long> requestIds = updateRequest.getRequestIds();
        List<Request> foundRequests = requestRepository.findAllById(requestIds);

        if (foundRequests.size() != requestIds.size()) {
            throw new NotFoundException("Некоторые запросы не найдены");
        }

        for (Request request : foundRequests) {
            if (request.getStatus() != RequestStatus.PENDING) {
                throw new ConditionsNotMetException("Заявка должна находиться в ожидании");
            }
        }

        RequestStatus newStatus = updateRequest.getStatus();

        if (newStatus == RequestStatus.CONFIRMED) {
            int currentConfirmed = getConfirmedRequestsCount(eventId);
            int participantLimit = event.getParticipantLimit();

            if (participantLimit > 0) {
                int availableSlots = participantLimit - currentConfirmed;

                if (availableSlots <= 0) {
                    throw new ConditionsNotMetException("Лимит участников уже достигнут");
                }

                if (requestIds.size() > availableSlots) {
                    throw new ConditionsNotMetException("Количество запросов превышает доступные места");
                }
            }
        }

        EventRequestStatusUpdateResult result = new EventRequestStatusUpdateResult();
        List<ParticipationRequestDto> confirmed = new ArrayList<>();
        List<ParticipationRequestDto> rejected = new ArrayList<>();

        switch (updateRequest.getStatus()) {
            case CONFIRMED -> handleConfirmedRequests(event, foundRequests, result, confirmed, rejected);
            case REJECTED -> handleRejectedRequests(foundRequests, rejected);
        }

        result.setConfirmedRequests(confirmed);
        result.setRejectedRequests(rejected);

        updateEventConfirmedRequestsCount(eventId);

        return result;
    }

    @Override
    @Transactional
    public ParticipationRequestDto cancelRequest(Long userId, Long requestId) {

        Request request = requestRepository.findById(requestId)
                .orElseThrow(() -> new NotFoundException(
                        String.format("Запрос на участие в событии с id запроса=%d не найден", requestId)));

        if (request.getRequesterId() != userId) {
            throw new ConditionsNotMetException("Пользователь не является участником в запросе на участие в событии");
        }

        if (request.getStatus() == CONFIRMED) {
            throw new ConditionsNotMetException("Нельзя отменить уже подтверждённую заявку на участие в событии");
        }

        request.setStatus(RequestStatus.CANCELED);
        Request updatedRequest = requestRepository.save(request);

        updateEventConfirmedRequestsCount(request.getEventId());

        return requestMapper.toDto(updatedRequest);
    }

    @Transactional
    protected Request saveRequest(EventForRequestDto event, long userId) {
        RequestStatus status;

        int currentConfirmed = getConfirmedRequestsCount(event.getId());
        int participantLimit = event.getParticipantLimit();

        Boolean requestModeration = event.getRequestModeration();

        if (participantLimit == 0) {
            status = CONFIRMED;
        }
        else if (!requestModeration && currentConfirmed < participantLimit) {
            status = CONFIRMED;
        }
        else {
            status = PENDING;
        }

        Request request = Request.builder()
                .eventId(event.getId())
                .requesterId(userId)
                .status(status)
                .created(LocalDateTime.now().truncatedTo(ChronoUnit.MILLIS))
                .build();

        return requestRepository.save(request);
    }

    private void checkParticipantLimit(int participantLimit, int confirmedRequests) {
        if (participantLimit > 0 && confirmedRequests >= participantLimit) {
            throw new ConditionsNotMetException("У события заполнен лимит участников");
        }
    }

    private int getConfirmedRequestsCount(long eventId) {
        return requestRepository.findCountOfConfirmedRequestsByEventId(eventId);
    }

    private void updateStatus(RequestStatus status, List<Long> ids) {
        if (ids != null && !ids.isEmpty()) {
            requestRepository.updateStatus(status, ids);
        }
    }

    private void updateEventConfirmedRequestsCount(Long eventId) {
        int newCount = getConfirmedRequestsCount(eventId);
        try {
            Map<Long, Integer> updates = new HashMap<>();
            updates.put(eventId, newCount);
            eventClient.updateConfirmedRequests(updates);
        } catch (Exception e) {
            log.warn("Could not update confirmed requests count in event service: {}", e.getMessage());
        }

    }

    private void handleConfirmedRequests(EventForRequestDto event, List<Request> foundRequests,
                                         EventRequestStatusUpdateResult result,
                                         List<ParticipationRequestDto> confirmed,
                                         List<ParticipationRequestDto> rejected) {
        int confirmedRequests = getConfirmedRequestsCount(event.getId());
        int participantLimit = event.getParticipantLimit();

        if (participantLimit == 0 || !event.getRequestModeration()) {
            result.setConfirmedRequests(requestMapper.toDtoList(foundRequests));
        }

        checkParticipantLimit(participantLimit, confirmedRequests);

        for (Request request : foundRequests) {
            if (confirmedRequests >= participantLimit) {
                rejected.add(requestMapper.toDto(request));
                continue;
            }
            request.setStatus(RequestStatus.CONFIRMED);
            confirmed.add(requestMapper.toDto(request));
            ++confirmedRequests;
        }

        List<Long> confirmedRequestIds = confirmed.stream().map(ParticipationRequestDto::getId).toList();
        updateStatus(RequestStatus.CONFIRMED, confirmedRequestIds);
    }

    private void handleRejectedRequests(List<Request> foundRequests, List<ParticipationRequestDto> rejected) {

        List<Long> rejectedIds = new ArrayList<>();
        for (Request request : foundRequests) {
            rejectedIds.add(request.getId());
            request.setStatus(RequestStatus.REJECTED);
            rejected.add(requestMapper.toDto(request));
        }

        updateStatus(REJECTED, rejectedIds);
    }
}