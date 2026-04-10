package ru.yandex.practicum.event.controller;

import ru.yandex.practicum.client.StatsClient;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.event.dto.EventResponseDto;
import ru.yandex.practicum.event.dto.EventShortDto;
import ru.yandex.practicum.event.service.EventService;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/events")
public class PublicEventController {
    private final EventService eventService;
    private final StatsClient client;

    @GetMapping
    public List<EventShortDto> getEvents(
            @RequestParam(required = false) String text,
            @RequestParam(required = false) List<Long> categories,
            @RequestParam(required = false) Boolean paid,
            @RequestParam(required = false) String rangeStart,
            @RequestParam(required = false) String rangeEnd,
            @RequestParam(defaultValue = "false") Boolean onlyAvailable,
            @RequestParam(required = false) String sort,
            @RequestParam(defaultValue = "0") int from,
            @RequestParam(defaultValue = "10") int size, HttpServletRequest request) {

        log.info("GET /events: text={}, categories={}, paid={}, sort={}, from={}, size={}",
                text, categories, paid, sort, from, size);

        client.hit(request);

        log.warn("HIT Public  КОНТРОЛЛЕРА РАБОТАЕТ");

        return eventService.getEventsByPublic(text, categories, paid, rangeStart,
                rangeEnd, onlyAvailable, sort, from, size);
    }

    @GetMapping("/{id}")
    public EventResponseDto getEvent(@PathVariable Long id, HttpServletRequest request) {
        log.info("GET /events/{}", id);

        client.hit(request);

        log.warn("HIT Public одного события  КОНТРОЛЛЕРА РАБОТАЕТ");
        return eventService.getEventById(id);
    }
}