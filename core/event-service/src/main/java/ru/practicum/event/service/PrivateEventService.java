package ru.practicum.event.service;

import ru.practicum.event.dto.EventRequestDto;
import ru.practicum.event.dto.EventResponseDto;
import ru.practicum.dto.event.EventShortDto;
import ru.practicum.event.dto.EventUpdateRequestDto;

import java.util.List;

public interface PrivateEventService {
    EventResponseDto createEvent(Long userId, EventRequestDto eventRequestDto);

    List<EventShortDto> getEventsByUser(Long userId, int from, int size);

    EventResponseDto getEventByUser(Long userId, Long eventId);

    EventResponseDto updateEventByUser(Long userId, Long eventId, EventUpdateRequestDto updateRequest);
}
