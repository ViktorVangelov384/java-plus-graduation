package teamfive.comment.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import teamfive.comment.dto.CommentDto;
import teamfive.comment.model.Comment;

@Mapper(componentModel = "spring")
public interface CommentMapper {

    @Mappings({
            @Mapping(source = "user.id", target = "userId"),
            @Mapping(source = "event.id", target = "eventId")
    })
    CommentDto toCommentDto(Comment comment);
}

