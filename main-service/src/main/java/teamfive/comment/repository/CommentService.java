package teamfive.comment.repository;

import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import teamfive.comment.dto.CommentDto;
import teamfive.comment.dto.InputCommentDto;
import teamfive.comment.mapper.CommentMapper;
import teamfive.comment.model.Comment;
import teamfive.comment.service.CommentRepository;
import teamfive.event.dto.EventResponseDto;
import teamfive.event.model.Event;
import teamfive.event.model.EventState;
import teamfive.event.service.EventService;
import teamfive.exception.ConflictException;
import teamfive.user.dto.UserDto;
import teamfive.user.model.User;
import teamfive.user.service.UserService;

import java.time.LocalDateTime;

@Service
@AllArgsConstructor
@Transactional(readOnly = true)
public class CommentService {
    private final CommentRepository repository;
    private final EventService eventService;
    private final UserService userService;
    private final CommentMapper mapper;

    @Transactional
    public CommentDto create(@Positive Long userId, InputCommentDto comment) {
        UserDto user = userService.get(userId);
        EventResponseDto event = eventService.getEventById(comment.getEventId());
        if (!event.getState().equals(EventState.PUBLISHED.toString()))
            throw new ConflictException("Событие не опубликовано. Комментарии запрещены");

        User relatedUser = new User();
        relatedUser.setId(user.getId());

        Event relatedEvent = new Event();
        relatedEvent.setId(event.getId());

        Comment addCom = new Comment();
        addCom.setUser(relatedUser);
        addCom.setEvent(relatedEvent);
        addCom.setText(comment.getText());
        addCom.setCreated(LocalDateTime.now());
        return mapper.toCommentDto(repository.save(addCom));
    }
}
