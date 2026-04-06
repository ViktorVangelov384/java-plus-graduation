package stats.request.service;

import stats.event.dto.EventRequestStatusUpdateRequest;
import stats.event.dto.EventResponseDto;
import stats.request.dto.EventRequestStatusUpdateResult;
import stats.request.dto.ParticipationRequestDto;

import java.util.List;

public interface RequestService {

    ParticipationRequestDto create(Long userId, Long eventId, EventResponseDto eventById);

    List<ParticipationRequestDto> getRequests(Long userId);

    ParticipationRequestDto cancelRequest(Long userId, Long requestId);

    List<ParticipationRequestDto> getRequestsForUserEvent(Long userId, Long eventId);

    EventRequestStatusUpdateResult updateRequestStatuses(Long userId, Long eventId,
                                                         EventRequestStatusUpdateRequest updateRequest);
}
