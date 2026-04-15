package ru.yandex.practicum.comment.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import ru.yandex.practicum.comment.dto.CommentDto;
import ru.yandex.practicum.comment.model.Comment;

@Mapper(componentModel = "spring")
public interface CommentMapper {

    @Mappings({
            @Mapping(source = "user.id", target = "userId"),
            @Mapping(source = "event.id", target = "eventId")
    })
    CommentDto toCommentDto(Comment comment);
}

