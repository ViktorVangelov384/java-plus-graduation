package stats.comment.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import stats.comment.dto.CommentDto;
import stats.comment.dto.InputCommentDto;
import stats.comment.dto.UpdateCommentDto;
import stats.comment.service.CommentService;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/user/{userId}/comments")
@RequiredArgsConstructor
@Validated
public class PrivateCommentController {
    private final CommentService service;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CommentDto create(@PathVariable @Positive Long userId,
                             @Valid @RequestBody InputCommentDto comment) {
        log.info("POST: Создание комментария. Входные параметры: userId={} comment={}", userId, comment);
        return service.create(userId, comment);
    }

    @DeleteMapping("/{commentId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteForOwner(@PathVariable @Positive Long userId,
                               @PathVariable Long commentId) {
        log.info("DELETE: Удаление комментария (Id={}) создателем (Id={})", userId, commentId);
        service.deleteForOwner(userId, commentId);
    }

    @PatchMapping("/{commentId}")
    public CommentDto update(@PathVariable @Positive Long userId,
                             @PathVariable Long commentId,
                             @Valid @RequestBody UpdateCommentDto updateCommentDto) {
        log.info("PATCH: Обновление комментария (Id={}) пользователем (Id={})", commentId, userId);
        return service.updateComment(commentId, userId, updateCommentDto);
    }

    @GetMapping
    public List<CommentDto> getUserComments(
            @PathVariable @Positive Long userId) {
        log.info("GET: Получение списка комментариев пользователя (Id={}", userId);
        return service.getAllForUser(userId);
    }
}

