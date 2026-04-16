package ru.practicum.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;
import java.util.Map;

@FeignClient(name = "request-service")
public interface RequestClient{

    @PostMapping("/internal/requests/count")
    Map<Long, Integer> getCountConfirmedRequestsByEventIds(@RequestBody List<Long> eventsIds);

    @GetMapping("/internal/requests/count/{eventId}")
    int getCountConfirmedRequestsByEventId(@PathVariable("eventId") Long eventId);
}
