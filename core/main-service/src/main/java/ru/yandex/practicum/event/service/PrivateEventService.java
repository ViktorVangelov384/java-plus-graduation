package ru.yandex.practicum.event.service;

import ru.yandex.practicum.event.dto.EventRequestDto;
import ru.yandex.practicum.event.dto.EventResponseDto;
import ru.yandex.practicum.event.dto.EventShortDto;
import ru.yandex.practicum.event.dto.EventUpdateRequestDto;

import java.util.List;

public interface PrivateEventService {
    EventResponseDto createEvent(Long userId, EventRequestDto eventRequestDto);

    List<EventShortDto> getEventsByUser(Long userId, int from, int size);

    EventResponseDto getEventByUser(Long userId, Long eventId);

    EventResponseDto updateEventByUser(Long userId, Long eventId, EventUpdateRequestDto updateRequest);
}
