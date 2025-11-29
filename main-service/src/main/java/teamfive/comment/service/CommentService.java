package teamfive.comment.service;

import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import teamfive.comment.dto.CommentDto;
import teamfive.comment.dto.InputCommentDto;
import teamfive.comment.mapper.CommentMapper;
import teamfive.comment.model.Comment;
import teamfive.comment.repository.CommentRepository;
import teamfive.event.dto.EventResponseDto;
import teamfive.event.model.Event;
import teamfive.event.model.EventState;
import teamfive.event.service.EventService;
import teamfive.exception.ConflictException;
import teamfive.exception.NotFoundException;
import teamfive.user.dto.UserDto;
import teamfive.user.model.User;
import teamfive.user.service.UserService;

import java.time.LocalDateTime;
import java.util.Optional;

@Slf4j
@Service
@AllArgsConstructor
@Transactional(readOnly = true)
public class CommentService {
    private final CommentRepository repository;
    private final EventService eventService;
    private final UserService userService;
    private final CommentMapper mapper;

    @Transactional
    public CommentDto create(Long userId, InputCommentDto comment) {
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
        log.debug("Коммент перед сохранением {}", addCom);
        return mapper.toCommentDto(repository.save(addCom));
    }

    @Transactional
    public void deleteByIdByAdmin(Long id) {
        checkExistsById(id);
        repository.deleteById(id);
        log.debug("Комментарий с id={}, удален администратором.", id);
    }

    @Transactional
    public void deleteForOwner(Long userId, Long commentId) {
        Optional<Comment> optionalComment = repository.findById(commentId);
        if (optionalComment.isEmpty()) {
            throw new NotFoundException("Комментарий с id={" + commentId + "} не найден");
        }

        Comment comment = optionalComment.get();
        if (!comment.getUser().getId().equals(userId)) {
            throw new ConflictException("Удаление не возможно. Обратитесь к администратору или владельцу.");
        }
        repository.deleteById(commentId);
        log.debug("Комментарий с id={}, удален владельцем.", commentId);
    }

    public void checkExistsById(Long id) {
        if(!repository.existsById(id))
            throw new NotFoundException("Комментарий с id={" + id + "} не найден");
    }
}
