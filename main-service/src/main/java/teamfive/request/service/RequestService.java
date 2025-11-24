package teamfive.request.service;

import teamfive.event.dto.EventResponseDto;
import teamfive.request.dto.ParticipationRequestDto;

public interface RequestService {

    ParticipationRequestDto create(Long userId, Long eventId, EventResponseDto eventById);

}
