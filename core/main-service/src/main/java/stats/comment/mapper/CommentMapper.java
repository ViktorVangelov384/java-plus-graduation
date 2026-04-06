package stats.comment.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import stats.comment.dto.CommentDto;
import stats.comment.model.Comment;

@Mapper(componentModel = "spring")
public interface CommentMapper {

    @Mappings({
            @Mapping(source = "user.id", target = "userId"),
            @Mapping(source = "event.id", target = "eventId")
    })
    CommentDto toCommentDto(Comment comment);
}

