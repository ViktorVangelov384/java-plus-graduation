package teamfive.request.service;

import teamfive.event.dto.EventResponseDto;
import teamfive.request.dto.ParticipationRequestDto;

import java.util.List;

public interface RequestService {

    ParticipationRequestDto create(Long userId, Long eventId, EventResponseDto eventById);

    List<ParticipationRequestDto> getRequests(Long userId);

    ParticipationRequestDto cancelRequest(Long userId, Long requestId);
}
