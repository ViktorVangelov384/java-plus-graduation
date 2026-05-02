package ru.practicum.event.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;
import ru.practicum.event.dto.EventResponseDto;
import ru.practicum.event.dto.EventUpdateRequestDto;
import ru.practicum.event.service.EventService;
import ru.practicum.stats.client.ActionType;
import ru.practicum.stats.client.CollectorGrpcClient;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/events")
public class AdminEventController {
    private final EventService eventService;
    private final CollectorGrpcClient collectorClient;

    @GetMapping
    public List<EventResponseDto> getEvents(
            @RequestParam(required = false) List<Long> users,
            @RequestParam(required = false) List<String> states,
            @RequestParam(required = false) List<Long> categories,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime rangeStart,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime rangeEnd,
            @RequestParam(defaultValue = "0") int from,
            @RequestParam(defaultValue = "10") int size, HttpServletRequest request) {

        log.info("GET /admin/events: users={}, states={}, categories={}, rangeStart={}, rangeEnd={}",
                users, states, categories, rangeStart, rangeEnd);

        String rangeStartStr = rangeStart != null ? rangeStart.format(DateTimeFormatter.ISO_DATE_TIME) : null;
        String rangeEndStr = rangeEnd != null ? rangeEnd.format(DateTimeFormatter.ISO_DATE_TIME) : null;

        try {
            collectorClient.sendUserAction(0L, 0L, ActionType.ACTION_VIEW);
        } catch (Exception e) {
            log.warn("Stats server unavailable: {}", e.getMessage());
        }

        return eventService.getEventsByAdmin(users, states, categories, rangeStartStr, rangeEndStr, from, size);
    }

    @PatchMapping("/{eventId}")
    public EventResponseDto updateEvent(@PathVariable Long eventId,
                                        @Valid @RequestBody EventUpdateRequestDto updateRequest) {
        log.info("PATCH /admin/events/{}", eventId);
        return eventService.updateEventByAdmin(eventId, updateRequest);
    }
}