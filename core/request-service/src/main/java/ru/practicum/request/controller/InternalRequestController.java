package ru.practicum.request.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.practicum.request.service.CountingService;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/internal/requests")
@Slf4j
public class InternalRequestController {

    private final CountingService requestCountingService;

    @PostMapping("/count")
    public Map<Long, Integer> getCountConfirmedRequestsByEventIds(@RequestBody List<Long> eventsIds) {
        log.info("Counting confirmed requests for events: {}", eventsIds);
        Map<Long, Integer> result = requestCountingService.getCountConfirmedRequestsByEventIds(eventsIds);
        log.info("Count result: {}", result);
        return result;
    }

    @GetMapping("/count/{eventId}")
    public int getCountConfirmedRequestsByEventId(@PathVariable Long eventId) {
        log.info("Counting confirmed requests for event: {}", eventId);
        int result = requestCountingService.getCountConfirmedRequestsByEventId(eventId);
        log.info("Count for event {}: {}", eventId, result);
        return result;
    }
}
