package teamfive.request.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import teamfive.event.dto.EventResponseDto;
import teamfive.event.model.EventState;
import teamfive.exception.ConflictException;
import teamfive.exception.DuplicatedException;
import teamfive.request.dto.ParticipationRequestDto;
import teamfive.request.enums.RequestStatus;
import teamfive.request.mapper.RequestMapper;
import teamfive.request.model.ParticipationRequest;
import teamfive.request.repository.RequestRepository;
import teamfive.user.dto.UserDto;
import teamfive.user.service.UserService;

import java.time.LocalDateTime;

@Slf4j
@Service
@AllArgsConstructor
@Transactional(readOnly = true)
public class RequestServiceImpl implements RequestService {

    private final RequestRepository repository;
    private final UserService userService;
    private final RequestMapper mapper;


    @Override
    @Transactional
    public ParticipationRequestDto create(Long userId, Long eventId, EventResponseDto event) {
        if (repository.findByEventIdAndRequesterId(eventId, userId).isPresent())
            throw new DuplicatedException("Такая заявка уже создана");

        if (event.getInitiator().getId().equals(userId))
            throw new ConflictException("Инициатор события не может добавить запрос на участие в своём событии");

        if (event.getParticipantLimit() != 0 && !event.getState().equals(EventState.PUBLISHED.toString()))
            throw new ConflictException("Нельзя участвовать в неопубликованном событии");

        int confirmedRequestsCount = repository.findAllByEventIdAndStatus(
                eventId,
                RequestStatus.CONFIRMED.toString()).size();

        if (event.getParticipantLimit() > 0 && confirmedRequestsCount >= event.getParticipantLimit())
            throw new ConflictException("Достигнут лимит запросов на участие");

        UserDto user = userService.get(userId);
        RequestStatus status = RequestStatus.PENDING;
        if (!event.getRequestModeration() || event.getParticipantLimit().equals(0)) {
            status = RequestStatus.CONFIRMED;
        }

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




}
