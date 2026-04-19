package ru.practicum.event.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.practicum.dto.event.EventForRequestDto;
import ru.practicum.event.service.EventService;

import java.util.Map;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/internal/events")
public class InternalEventController {

    private final EventService eventService;

    @GetMapping("/{eventId}")
    public EventForRequestDto getEventForRequest(@PathVariable Long eventId) {
        log.info("GET /internal/events/{} - Internal request", eventId);
        return eventService.getEventForRequest(eventId);
    }

    @GetMapping("/check/{userId}/{eventId}")
    public boolean checkUserIsInitiator(@PathVariable Long userId, @PathVariable Long eventId) {
        log.info("GET /internal/events/check/{}/{} - Checking initiator", userId, eventId);
        return eventService.isUserInitiator(userId, eventId);
    }

    @PostMapping("/confirmed-requests")
    public void updateConfirmedRequests(@RequestBody Map<Long, Integer> updates) {
        log.info("POST /internal/events/confirmed-requests - Updates: {}", updates);
        eventService.updateConfirmedRequestsCounts(updates);
    }
}