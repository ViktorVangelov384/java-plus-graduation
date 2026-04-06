package stats.event.controller;

import client.StatsClientService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;
import ru.practicum.dto.StatDto;
import stats.event.dto.EventResponseDto;
import stats.event.dto.EventUpdateRequestDto;
import stats.event.service.EventService;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/events")
public class AdminEventController {
    private final EventService eventService;
    private final StatsClientService statsClientService;

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

        StatDto statDto = StatDto.builder()
                .uri(request.getRequestURI())
                .ip(request.getRemoteAddr())
                .timestamp(LocalDateTime.now())
                .app("ewm-main-service")
                .build();

        String rangeStartStr = rangeStart != null ? rangeStart.format(DateTimeFormatter.ISO_DATE_TIME) : null;
        String rangeEndStr = rangeEnd != null ? rangeEnd.format(DateTimeFormatter.ISO_DATE_TIME) : null;

        statsClientService.hit(statDto);
        log.warn("HIT АДМИН КОНТРОЛЛЕРА РАБОТАЕТ");

        return eventService.getEventsByAdmin(users, states, categories, rangeStartStr, rangeEndStr, from, size);
    }

    @PatchMapping("/{eventId}")
    public EventResponseDto updateEvent(@PathVariable Long eventId,
                                        @Valid @RequestBody EventUpdateRequestDto updateRequest) {
        log.info("PATCH /admin/events/{}", eventId);
        return eventService.updateEventByAdmin(eventId, updateRequest);
    }
}
