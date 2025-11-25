package teamfive.request.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import teamfive.event.dto.EventResponseDto;
import teamfive.event.model.EventState;
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


    @Override
    @Transactional
    public ParticipationRequestDto create(Long userId, Long eventId, EventResponseDto event) {
        if (repository.findByEventIdAndRequesterId(eventId, userId).isPresent())
            throw new DuplicatedException("Такая заявка уже создана");

        if (event.getInitiator().getId().equals(userId))
            throw new ConflictException("Инициатор события не может добавить запрос на участие в своём событии");

        if (event.getParticipantLimit() != 0 && !event.getState().equals(EventState.PUBLISHED.toString()))
            throw new ConflictException("Нельзя участвовать в неопубликованном событии");

        int confirmedRequestsCount = repository.findAllByEventIdAndStatus(eventId, RequestStatus.CONFIRMED.toString()).size();

        if (event.getParticipantLimit() > 0 && confirmedRequestsCount >= event.getParticipantLimit())
            throw new ConflictException("Достигнут лимит запросов на участие");

        UserDto user = userService.get(userId);
        RequestStatus status = RequestStatus.PENDING;
        if (!event.getRequestModeration() || event.getParticipantLimit().equals(0)) {
            status = RequestStatus.CONFIRMED;
        }

        ParticipationRequest request = ParticipationRequest.builder().requesterId(user.getId()).eventId(event.getId()).status(status.toString()).created(LocalDateTime.now()).build();

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
}
