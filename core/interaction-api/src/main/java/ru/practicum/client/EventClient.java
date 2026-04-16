package ru.practicum.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;
import ru.practicum.dto.event.EventForRequestDto;

import java.util.Map;

@FeignClient(name = "event-service")
public interface EventClient {

    @GetMapping("/internal/events/{eventId}")
    EventForRequestDto getById(@PathVariable("eventId") Long eventId);

    @GetMapping("/internal/events/check/{userId}/{eventId}")
    boolean checkUserIsInitiator(@PathVariable("userId") Long userId,
                                 @PathVariable("eventId") Long eventId);

    @PostMapping("/internal/events/confirmed-requests")
    void updateConfirmedRequests(@RequestBody Map<Long, Integer> updates);

}