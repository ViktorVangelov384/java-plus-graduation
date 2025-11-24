package teamfive.event.dto;

import lombok.Data;
import teamfive.category.dto.OutputCategoryDto;
import teamfive.user.dto.UserDto;
import java.time.LocalDateTime;

@Data
public class EventShortDto {
    private Long id;
    private String annotation;
    private OutputCategoryDto category;
    private Integer confirmedRequests;
    private LocalDateTime eventDate;
    private UserDto initiator;
    private Boolean paid;
    private String title;
    private Long views;
}
