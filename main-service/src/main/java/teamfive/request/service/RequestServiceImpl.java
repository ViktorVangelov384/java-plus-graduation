package teamfive.request.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
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

        try {
            UserDto user = userService.get(userId);
            if (user == null) {
                throw new NotFoundException("Пользователь с id=" + userId + " не найден");
            }

            if (repository.findByEventIdAndRequesterId(eventId, userId).isPresent()) {
                throw new DuplicatedException("Такая заявка уже создана");
            }

            if (event.getInitiator() == null || event.getInitiator().getId() == null) {
                throw new ConflictException("Некорректные данные инициатора события");
            }

            if (event.getInitiator().getId().equals(userId)) {
                throw new ConflictException("Инициатор события не может добавить запрос на участие в своём событии");
            }

            if (!EventState.PUBLISHED.toString().equals(event.getState())) {
                throw new ConflictException("Нельзя участвовать в неопубликованном событии");
            }

            Integer participantLimit = event.getParticipantLimit() != null ? event.getParticipantLimit() : 0;
            Boolean requestModeration = event.getRequestModeration() != null ? event.getRequestModeration() : true;

            int confirmedCount = repository.findAllByEventIdAndStatus(eventId, RequestStatus.CONFIRMED.toString()).size();

            RequestStatus status;

            if (event.getParticipantLimit() == 0) {
                status = RequestStatus.CONFIRMED;
            } else if (event.getRequestModeration() == null || !event.getRequestModeration()) {
                if (confirmedCount < event.getParticipantLimit()) {
                    status = RequestStatus.CONFIRMED;
                } else {
                    throw new ConflictException("Достигнут лимит участников");
                }
            } else {
                if (confirmedCount < event.getParticipantLimit()) {
                    status = RequestStatus.PENDING;
                } else {
                    throw new ConflictException("Достигнут лимит участников");
                }

                if (!requestModeration) {
                    status = RequestStatus.CONFIRMED;
                    log.info("Модерация отключена - статус CONFIRMED");
                } else {
                    status = RequestStatus.PENDING;
                    log.info("Требуется модерация - статус PENDING");
                }
            }

            ParticipationRequest request = ParticipationRequest.builder()
                    .requesterId(userId)
                    .eventId(eventId)
                    .status(status.toString())
                    .created(LocalDateTime.now())
                    .build();

            ParticipationRequest savedRequest = repository.save(request);

            ParticipationRequestDto result = mapper.toDtoSafe(savedRequest);
            if (result == null) {
                throw new RuntimeException("Ошибка преобразования данных запроса");
            }

            return result;

        } catch (NotFoundException | ConflictException | DuplicatedException e) {
            throw e;
        } catch (DataAccessException e) {
            log.error("Ошибка доступа к данным при создании запроса: {}", e.getMessage(), e);
            throw new RuntimeException("Ошибка базы данных при создании заявки");
        } catch (Exception e) {
            log.error("Неожиданная ошибка при создании заявки: {}", e.getMessage(), e);
            throw new RuntimeException("Внутренняя ошибка сервера при создании заявки");
        }
    }

    @Override
    public List<ParticipationRequestDto> getRequests(Long userId) {
        try {
            log.info("Получение заявок пользователя: userId={}", userId);

            UserDto user = userService.get(userId);
            if (user == null) {
                throw new NotFoundException("Пользователь с id=" + userId + " не найден");
            }

            List<ParticipationRequest> requests = repository.findAllByRequesterId(userId);
            log.info("Найдено {} заявок для пользователя {}", requests.size(), userId);

            List<ParticipationRequestDto> result = requests.stream()
                    .map(mapper::toDtoSafe)
                    .filter(dto -> dto != null)
                    .collect(Collectors.toList());

            return requests.stream()
                    .map(mapper::toDto)
                    .collect(Collectors.toList());

        } catch (NotFoundException e) {
            throw e;
        } catch (Exception e) {
            log.error("Неожиданная ошибка при получении заявок пользователя {}: {}", userId, e.getMessage(), e);
            throw new RuntimeException("Внутренняя ошибка сервера при получении заявок");
        }
    }

    @Override
    @Transactional
    public ParticipationRequestDto cancelRequest(Long userId, Long requestId) {

        try {
            ParticipationRequest request = repository.findById(requestId)
                    .orElseThrow(() -> new NotFoundException("Заявка не найдена"));

            log.info("Найдена заявка: requesterId={}, status={}", request.getRequesterId(), request.getStatus());

            if (!request.getRequesterId().equals(userId)) {
                log.error("Попытка отменить чужую заявку: userId={}, заявка принадлежит userId={}",
                        userId, request.getRequesterId());
                throw new ConflictException("Пользователь, который не является автором заявки, не может её отменить.");
            }

            String currentStatus = request.getStatus();
            if (RequestStatus.CANCELED.toString().equals(currentStatus)) {
                log.info("Заявка {} уже отменена", requestId);
                return mapper.toDtoSafe(request);
            }

            log.info("Статус заявки изменен: requestId={}, oldStatus={}, newStatus=CANCELED",
                    requestId, currentStatus);

            request.setStatus(RequestStatus.CANCELED.toString());
            ParticipationRequest updatedRequest = repository.save(request);
            log.info("Статус заявки изменен: requestId={}, oldStatus={}, newStatus=CANCELED",
                    requestId, currentStatus);

            ParticipationRequestDto result = mapper.toDtoSafe(updatedRequest);
            if (result == null) {
                throw new RuntimeException("Ошибка преобразования данных отмененной заявки");
            }

            log.info("Участие в событии для пользователя с id={} отменено", userId);
            return result;

        } catch (NotFoundException | ConflictException e) {
            throw e;
        } catch (Exception e) {
            log.error("Неожиданная ошибка при отмене заявки {}: {}", requestId, e.getMessage(), e);
            throw new RuntimeException("Внутренняя ошибка сервера при отмене заявки");
        }
    }

    @Override
    public List<ParticipationRequestDto> getRequestsForUserEvent(Long userId, Long eventId) {
        log.info("Получение запросов на участие для события: userId={}, eventId={}", userId, eventId);
        try {
            List<ParticipationRequest> requests = repository.findAllByEventId(eventId);
            log.debug("Найдено запросов для события {}: {}", eventId, requests.size());
            List<ParticipationRequestDto> result = requests.stream()
                    .map(mapper::toDtoSafe)
                    .filter(dto -> dto != null)
                    .collect(Collectors.toList());

            return result;

        } catch (DataAccessException e) {
            log.error("Ошибка доступа к данным при получении запросов для события {}: {}", eventId, e.getMessage(), e);
            throw new RuntimeException("Ошибка базы данных при получении запросов события");
        } catch (Exception e) {
            log.error("Неожиданная ошибка при получении запросов для события {}: {}", eventId, e.getMessage(), e);
            throw new RuntimeException("Внутренняя ошибка сервера при получении запросов события");
        }
    }


@Override
@Transactional
public List<ParticipationRequestDto> updateRequestStatuses(Long userId, Long eventId,
                                                           EventRequestStatusUpdateRequest updateRequest) {
    log.info("Обновление статусов запросов: userId={}, eventId={}, requestIds={}, status={}",
            userId, eventId, updateRequest.getRequestIds(), updateRequest.getStatus());

    EventResponseDto event = eventService.getEventByIdForInternalUse(eventId);

    if (event.getInitiator() == null || !event.getInitiator().getId().equals(userId)) {
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

