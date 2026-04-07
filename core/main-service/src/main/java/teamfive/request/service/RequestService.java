package teamfive.request.service;

import teamfive.event.dto.EventRequestStatusUpdateRequest;
import teamfive.event.dto.EventResponseDto;
import teamfive.request.dto.EventRequestStatusUpdateResult;
import teamfive.request.dto.ParticipationRequestDto;

import java.util.List;

public interface RequestService {

    ParticipationRequestDto create(Long userId, Long eventId, EventResponseDto eventById);

    List<ParticipationRequestDto> getRequests(Long userId);

    ParticipationRequestDto cancelRequest(Long userId, Long requestId);

    List<ParticipationRequestDto> getRequestsForUserEvent(Long userId, Long eventId);

    EventRequestStatusUpdateResult updateRequestStatuses(Long userId, Long eventId,
                                                         EventRequestStatusUpdateRequest updateRequest);
}
