package ru.yandex.practicum.event.service;

import ru.yandex.practicum.event.dto.EventResponseDto;
import ru.yandex.practicum.event.dto.EventShortDto;
import ru.yandex.practicum.event.dto.EventUpdateRequestDto;

import java.util.List;

public interface EventService {

    List<EventResponseDto> getEventsByAdmin(List<Long> users, List<String> states,
                                            List<Long> categories, String rangeStart,
                                            String rangeEnd, int from, int size);

    EventResponseDto updateEventByAdmin(Long eventId, EventUpdateRequestDto updateRequest);

    List<EventShortDto> getEventsByPublic(String text, List<Long> categories,
                                          Boolean paid, String rangeStart,
                                          String rangeEnd, Boolean onlyAvailable,
                                          String sort, int from, int size);

    EventResponseDto getEventById(Long id);

    EventResponseDto getEventByIdForInternalUse(Long id);

}