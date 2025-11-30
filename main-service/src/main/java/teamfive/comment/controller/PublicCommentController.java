package teamfive.comment.controller;

import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import teamfive.comment.dto.CommentDto;
import teamfive.comment.service.CommentService;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/comments")
@RequiredArgsConstructor
@Validated
public class PublicCommentController {
    private final CommentService service;

    @GetMapping("/event/{eventId}")
    public List<CommentDto> get(@PathVariable @Positive Long eventId) {
        log.info("GET: Получение списка комментариев события (Id={}", eventId);
        return service.getByEventId(eventId);
    }
}
