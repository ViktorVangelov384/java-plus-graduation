package ru.practicum.request.controller;

import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.dto.event.EventRequestStatusUpdateRequest;
import ru.practicum.request.dto.EventRequestStatusUpdateResult;
import ru.practicum.request.dto.ParticipationRequestDto;
import ru.practicum.request.service.RequestService;

import java.util.List;

@Slf4j
@RestController
@AllArgsConstructor
@RequestMapping("/users/{userId}/events/{eventId}/requests")
@Validated
public class EventRequestController {

    private final RequestService requestService;

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<List<ParticipationRequestDto>> getRequestsForUserEvent(
            @PathVariable @Positive Long userId,
            @PathVariable @Positive Long eventId) {

        log.info("GET: Получение запросов на участие в событии пользователя. userId={}, eventId={}", userId, eventId);

        List<ParticipationRequestDto> requests = requestService.getRequestsForUserEvent(userId, eventId);
        return ResponseEntity.ok(requests);
    }

    @PatchMapping
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<EventRequestStatusUpdateResult> updateEventRequests(
            @PathVariable @Positive Long userId,
            @PathVariable @Positive Long eventId,
            @RequestBody EventRequestStatusUpdateRequest updateRequest) {

        log.info("PATCH: Обновление запросов на участие в событии. userId={}, eventId={}, updateRequest={}",
                userId, eventId, updateRequest);

        EventRequestStatusUpdateResult updatedRequests = requestService.updateRequestStatus(userId, eventId, updateRequest);
        return ResponseEntity.ok(updatedRequests);
    }
}
