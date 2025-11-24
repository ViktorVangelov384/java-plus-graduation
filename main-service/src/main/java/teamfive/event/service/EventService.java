package teamfive.event.service;

import teamfive.event.dto.EventResponseDto;
import teamfive.event.dto.EventShortDto;
import teamfive.event.dto.EventUpdateRequestDto;

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
}