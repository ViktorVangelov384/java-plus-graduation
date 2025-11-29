package teamfive.comment.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import teamfive.comment.dto.CommentDto;
import teamfive.comment.dto.InputCommentDto;
import teamfive.comment.repository.CommentService;

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
        return service.create(userId, comment);
    }

}
