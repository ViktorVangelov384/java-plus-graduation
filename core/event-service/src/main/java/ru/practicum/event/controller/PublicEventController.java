package ru.practicum.event.controller;

import org.springframework.format.annotation.DateTimeFormat;
import ru.yandex.practicum.client.StatsClient;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.practicum.event.dto.EventResponseDto;
import ru.practicum.dto.event.EventShortDto;
import ru.practicum.event.service.EventService;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/events")
public class PublicEventController {
    private final EventService eventService;
    private final StatsClient client;

    @GetMapping
    public List<EventShortDto> getAll(@RequestParam(required = false) String text,
                                      @RequestParam(required = false) List<Long> categories,
                                      @RequestParam(required = false) Boolean paid,
                                      @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime rangeStart,
                                      @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime rangeEnd,
                                      @RequestParam(required = false) Boolean onlyAvailable,
                                      @RequestParam(required = false) String sort,
                                      @RequestParam(defaultValue = "0") int from,
                                      @RequestParam(defaultValue = "10") int size,
                                      HttpServletRequest request) {

        String rangeStartStr = rangeStart != null ? rangeStart.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) : null;
        String rangeEndStr = rangeEnd != null ? rangeEnd.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) : null;

        List<EventShortDto> events = eventService.getEventsByPublic(
                text, categories, paid, rangeStartStr, rangeEndStr,
                onlyAvailable, sort, from, size);

        try {
            client.hit(request);
        } catch (Exception e) {
            log.warn("Stats server unavailable: {}", e.getMessage());
        }

        return events;
    }

    @GetMapping("/{id}")
    public EventResponseDto getEvent(@PathVariable Long id, HttpServletRequest request) {
        log.info("GET /events/{}", id);

        client.hit(request);

        return eventService.getEventById(id);
    }
}