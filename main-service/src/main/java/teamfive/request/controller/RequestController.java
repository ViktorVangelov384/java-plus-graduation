package teamfive.request.controller;

import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import teamfive.event.service.EventService;
import teamfive.request.dto.ParticipationRequestDto;
import teamfive.request.service.RequestService;

@Slf4j
@RestController
@AllArgsConstructor
@RequestMapping("/users/{userId}/requests")
@Validated
public class RequestController {

    private final RequestService requestService;
    private final EventService eventService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ParticipationRequestDto create(@PathVariable @Positive Long userId,
                                          @RequestParam @Positive Long eventId) {
        log.info("POST: Создание запроса. Параметры ID пользователя: {}, ID события: {}", userId, eventId);
        return requestService.create(userId, eventId, eventService.getEventById(eventId));
    }





}
