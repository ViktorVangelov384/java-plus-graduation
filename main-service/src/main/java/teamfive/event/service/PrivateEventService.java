package teamfive.event.service;

import teamfive.event.dto.EventRequestDto;
import teamfive.event.dto.EventResponseDto;
import teamfive.event.dto.EventShortDto;
import teamfive.event.dto.EventUpdateRequestDto;

import java.util.List;

public interface PrivateEventService {
    EventResponseDto createEvent(Long userId, EventRequestDto eventRequestDto);

    List<EventShortDto> getEventsByUser(Long userId, int from, int size);

    EventResponseDto getEventByUser(Long userId, Long eventId);

    EventResponseDto updateEventByUser(Long userId, Long eventId, EventUpdateRequestDto updateRequest);
}
