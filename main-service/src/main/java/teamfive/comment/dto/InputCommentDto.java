package teamfive.comment.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class InputCommentDto {
    @Positive
    @NotNull
    private Long eventId;

    @NotNull
    @Size(min = 1, max = 1000)
    private String text;
}
