package ru.practicum.event.service;

import ru.practicum.dto.event.EventForRequestDto;
import ru.practicum.event.dto.EventResponseDto;
import ru.practicum.dto.event.EventShortDto;
import ru.practicum.event.dto.EventUpdateRequestDto;

import java.util.List;
import java.util.Map;

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

    EventForRequestDto getEventForRequest(Long eventId);

    boolean isUserInitiator(Long userId, Long eventId);

    void updateConfirmedRequestsCounts(Map<Long, Integer> updates);


}