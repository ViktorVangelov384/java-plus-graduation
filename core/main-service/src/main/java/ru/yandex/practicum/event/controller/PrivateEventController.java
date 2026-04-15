package ru.yandex.practicum.event.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.event.dto.EventRequestDto;
import ru.yandex.practicum.event.dto.EventResponseDto;
import ru.yandex.practicum.event.dto.EventShortDto;
import ru.yandex.practicum.event.dto.EventUpdateRequestDto;
import ru.yandex.practicum.event.service.PrivateEventService;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/users/{userId}/events")
public class PrivateEventController {
    private final PrivateEventService privateEventService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public EventResponseDto createEvent(@PathVariable Long userId,
                                        @Valid @RequestBody EventRequestDto eventRequestDto) {
        log.info("POST /users/{}/events: title={}", userId, eventRequestDto.getTitle());
        return privateEventService.createEvent(userId, eventRequestDto);
    }

    @GetMapping
    public List<EventShortDto> getEventsByUser(@PathVariable Long userId,
                                               @RequestParam(defaultValue = "0") int from,
                                               @RequestParam(defaultValue = "10") int size) {
        log.info("GET /users/{}/events: from={}, size={}", userId, from, size);
        return privateEventService.getEventsByUser(userId, from, size);
    }

    @GetMapping("/{eventId}")
    public EventResponseDto getEventByUser(@PathVariable Long userId,
                                           @PathVariable Long eventId) {
        log.info("GET /users/{}/events/{}", userId, eventId);
        return privateEventService.getEventByUser(userId, eventId);
    }

    @PatchMapping("/{eventId}")
    public EventResponseDto updateEventByUser(@PathVariable Long userId,
                                              @PathVariable Long eventId,
                                              @Valid @RequestBody EventUpdateRequestDto updateRequest) {
        log.info("PATCH /users/{}/events/{}", userId, eventId);
        return privateEventService.updateEventByUser(userId, eventId, updateRequest);
    }
}