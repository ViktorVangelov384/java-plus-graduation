package stats.event.service;

import stats.event.dto.EventRequestDto;
import stats.event.dto.EventResponseDto;
import stats.event.dto.EventShortDto;
import stats.event.dto.EventUpdateRequestDto;

import java.util.List;

public interface PrivateEventService {
    EventResponseDto createEvent(Long userId, EventRequestDto eventRequestDto);

    List<EventShortDto> getEventsByUser(Long userId, int from, int size);

    EventResponseDto getEventByUser(Long userId, Long eventId);

    EventResponseDto updateEventByUser(Long userId, Long eventId, EventUpdateRequestDto updateRequest);
}
