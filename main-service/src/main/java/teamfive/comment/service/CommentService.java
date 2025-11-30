package teamfive.comment.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import teamfive.comment.dto.CommentDto;
import teamfive.comment.dto.InputCommentDto;
import teamfive.comment.dto.UpdateCommentDto;
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
import java.util.List;

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
    public CommentDto create(Long userId, InputCommentDto commentDto) {
        UserDto user = userService.get(userId);
        EventResponseDto event = eventService.getEventById(commentDto.getEventId());

        validateEventState(event);

        Comment comment = buildComment(user, event, commentDto.getText());
        log.debug("Комментарий перед сохранением: {}", comment);

        return mapper.toCommentDto(repository.save(comment));
    }

    @Transactional
    public void deleteByIdByAdmin(Long id) {
        checkExistsById(id);
        repository.deleteById(id);
        log.debug("Комментарий с id={} удален администратором", id);
    }

    @Transactional
    public void deleteForOwner(Long userId, Long commentId) {
        Comment comment = findCommentById(commentId);

        if (!comment.getUser().getId().equals(userId)) {
            throw new ConflictException("Удаление невозможно. Обратитесь к администратору или владельцу");
        }

        repository.deleteById(commentId);
        log.debug("Комментарий с id={} удален владельцем", commentId);
    }

    public void checkExistsById(Long id) {
        if (!repository.existsById(id)) {
            throw new NotFoundException("Комментарий с id={" + id + "} не найден");
        }
    }

    public List<CommentDto> getByEventId(Long eventId) {
        EventResponseDto eventResponseDto = eventService.getEventById(eventId);
        return repository.getAllByEventId(eventId)
                .stream()
                .map(mapper::toCommentDto)
                .toList();
    }

    public List<CommentDto> getAllForUser(Long userId) {
        UserDto user = userService.get(userId);
        return repository.getAllByUserId(userId)
                .stream()
                .map(mapper::toCommentDto)
                .toList();
    }

    @Transactional
    public CommentDto updateComment(Long commentId, Long userId, UpdateCommentDto dto) {
        checkExistsById(commentId);
        Comment existingComment = findCommentById(commentId);

        validateCommentOwnership(userId, existingComment);

        if (dto.getText() != null) {
            existingComment.setText(dto.getText());
            repository.save(existingComment);
        }

        return get(commentId);
    }

    public CommentDto get(Long id) {
        return repository.findById(id)
                .map(mapper::toCommentDto)
                .orElseThrow(() -> new NotFoundException("Комментарий с id={" + id + "} не найден"));
    }

    private void validateEventState(EventResponseDto event) {
        if (!event.getState().equals(EventState.PUBLISHED.toString())) {
            throw new ConflictException("Событие не опубликовано. Комментарии запрещены");
        }
    }

    private Comment buildComment(UserDto user, EventResponseDto event, String text) {
        User relatedUser = new User();
        relatedUser.setId(user.getId());

        Event relatedEvent = new Event();
        relatedEvent.setId(event.getId());

        Comment comment = new Comment();
        comment.setUser(relatedUser);
        comment.setEvent(relatedEvent);
        comment.setText(text);
        comment.setCreated(LocalDateTime.now());

        return comment;
    }

    private Comment findCommentById(Long commentId) {
        return repository.findById(commentId)
                .orElseThrow(() -> new NotFoundException("Комментарий с id={" + commentId + "} не найден"));
    }

    private void validateCommentOwnership(Long userId, Comment comment) {
        if (!comment.getUser().getId().equals(userId)) {
            throw new ConflictException("Обновлять комментарий может только автор!");
        }
    }
}