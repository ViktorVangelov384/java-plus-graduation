package teamfive.comment.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import teamfive.comment.dto.CommentDto;
import teamfive.comment.dto.InputCommentDto;
import teamfive.comment.service.CommentService;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/comments")
@RequiredArgsConstructor
@Validated
public class CommentController {
    private final CommentService service;

    @PostMapping("/user/{userId}")
    @ResponseStatus(HttpStatus.CREATED)
    public CommentDto create(@PathVariable @Positive Long userId,
                                    @Valid @RequestBody InputCommentDto comment) {
        log.info("POST: Создание комментария. Входные параметры: userId={} comment={}", userId, comment);
        return service.create(userId, comment);
    }

    @DeleteMapping("/admin/{commentId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteForAdmin(@PathVariable @Positive Long commentId) {
        log.info("DELETE: Удаление комментария (Id={}) администратором.", commentId);
        service.deleteByIdByAdmin(commentId);
    }

    @DeleteMapping("/user/{userId}/comment/{commentId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteForOwner(@PathVariable @Positive Long userId,
                               @PathVariable Long commentId) {
        log.info("DELETE: Удаление комментария (Id={}) создателем (Id={})", userId, commentId);
        service.deleteForOwner(userId, commentId);
    }

    @GetMapping("/event/{eventId}")
    public List<CommentDto> get(@PathVariable @Positive Long eventId) {
        log.info("GET: Получение списка комментариев события (Id={}", eventId);
        return service.get(eventId);
    }

    @GetMapping("/user/{userId}")
    public List<CommentDto> getUserComments(
            @PathVariable @Positive Long userId) {
        return service.getAllForUser(userId);
    }

    @GetMapping
    public List<CommentDto> getAllComments(
            @RequestParam(defaultValue = "0") @PositiveOrZero Integer from,
            @RequestParam(defaultValue = "10") @Positive Integer size) {
        return service.getAll(from, size);
    }
}
