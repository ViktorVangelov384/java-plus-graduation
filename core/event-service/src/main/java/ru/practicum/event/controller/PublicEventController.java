package ru.practicum.event.controller;

import org.springframework.format.annotation.DateTimeFormat;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.practicum.event.dto.EventResponseDto;
import ru.practicum.dto.event.EventShortDto;
import ru.practicum.event.dto.RecommendationEventDto;
import ru.practicum.event.service.EventService;
import ru.practicum.stats.client.ActionType;
import ru.practicum.stats.client.AnalyzerGrpcClient;
import ru.practicum.stats.client.CollectorGrpcClient;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/events")
public class PublicEventController {
    private final EventService eventService;
    private final CollectorGrpcClient collectorClient;
    private final AnalyzerGrpcClient analyzerClient;

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
            collectorClient.sendUserAction(0L, 0L, ActionType.ACTION_VIEW);
        } catch (Exception e) {
            log.warn("Stats server unavailable: {}", e.getMessage());
        }

        return events;
    }

    @GetMapping("/{id}")
    public EventResponseDto getEvent(@PathVariable Long id, HttpServletRequest request) {
        log.info("GET /events/{}", id);

        try {
            collectorClient.sendUserAction(0L, id, ActionType.ACTION_VIEW);
        } catch (Exception e) {
            log.warn("Stats server unavailable: {}", e.getMessage());
        }

        return eventService.getEventById(id);
    }

    @GetMapping("/recommendations")
    public List<RecommendationEventDto> getRecommendations(
            @RequestHeader("X-EWM-USER-ID") long userId,
            @RequestParam(defaultValue = "10") int maxResults) {

        log.info("GET /events/recommendations - userId={}, maxResults={}", userId, maxResults);

        return analyzerClient.getRecommendationsForUser(userId, maxResults)
                .map(proto -> new RecommendationEventDto(proto.getEventId(), proto.getScore()))
                .collect(Collectors.toList());
    }

    @GetMapping("/recommendations/similar")
    public List<RecommendationEventDto> getSimilarEvents(
            @RequestHeader("X-EWM-USER-ID") long userId,
            @RequestParam long eventId,
            @RequestParam(defaultValue = "10") int maxResults) {

        log.info("GET /events/recommendations/similar - userId={}, eventId={}, maxResults={}",
                userId, eventId, maxResults);

        return analyzerClient.getSimilarEvents(eventId, userId, maxResults)
                .map(proto -> new RecommendationEventDto(proto.getEventId(), proto.getScore()))
                .collect(Collectors.toList());
    }

}