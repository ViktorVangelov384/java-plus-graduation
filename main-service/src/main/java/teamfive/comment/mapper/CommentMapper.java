package teamfive.comment.mapper;

import org.mapstruct.Mapper;
import teamfive.comment.dto.CommentDto;
import teamfive.comment.model.Comment;

@Mapper(componentModel = "spring")
public interface CommentMapper {

    CommentDto toCommentDto(Comment comment);

}
