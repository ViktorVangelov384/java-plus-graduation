package teamfive.comment.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import teamfive.comment.dto.CommentDto;
import teamfive.comment.dto.InputCommentDto;
import teamfive.comment.service.CommentService;

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
    public void deleteForAdmin(@PathVariable @Positive Long id) {
        log.info("DELETE: Удаление комментария (Id={}) администратором.", id);
        service.deleteByIdByAdmin(id);
    }

    @DeleteMapping("/user/{userId}/comment/{commentId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteForOwner(@PathVariable @Positive Long userId,
                               @PathVariable Long commentId) {
        log.info("DELETE: Удаление комментария (Id={}) создателем (Id={})", userId, commentId);
        service.deleteByIdByOwner(userId, commentId);
    }


}
