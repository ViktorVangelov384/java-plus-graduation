package ru.practicum.request.controller;

import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.request.dto.ParticipationRequestDto;
import ru.practicum.request.service.RequestService;

import java.util.List;

@Slf4j
@RestController
@AllArgsConstructor
@RequestMapping("/users/{userId}/requests")
@Validated
public class RequestController {

    private final RequestService requestService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ParticipationRequestDto create(@PathVariable @Positive Long userId,
                                          @RequestParam @Positive Long eventId) {

        log.info("POST: Создание запроса. userId={}, eventId={}", userId, eventId);
        return requestService.create(userId, eventId);
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<ParticipationRequestDto> getRequests(@PathVariable @Positive Long userId) {
        log.info("GET: Получение заявок userId={}", userId);
        return requestService.getRequests(userId);
    }

    @PatchMapping("/{requestId}/cancel")
    @ResponseStatus(HttpStatus.OK)
    public ParticipationRequestDto cancelRequest(@PathVariable @Positive Long userId,
                                                 @PathVariable @Positive Long requestId) {
        log.info("PATCH: Отмена участия userId={}, requestId={}", userId, requestId);
        return requestService.cancelRequest(userId, requestId);
    }
}
