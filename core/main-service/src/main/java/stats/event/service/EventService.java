package stats.event.service;

import stats.event.dto.EventResponseDto;
import stats.event.dto.EventShortDto;
import stats.event.dto.EventUpdateRequestDto;

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