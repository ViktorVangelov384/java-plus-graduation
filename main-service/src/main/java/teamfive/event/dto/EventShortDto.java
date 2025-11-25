package teamfive.event.dto;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Data;
import teamfive.category.dto.OutputCategoryDto;
import teamfive.configuration.CustomLocalDateTimeDeserializer;
import teamfive.configuration.CustomLocalDateTimeSerializer;
import teamfive.user.dto.UserDto;
import java.time.LocalDateTime;

@Data
public class EventShortDto {
    private Long id;
    private String annotation;
    private OutputCategoryDto category;
    private Integer confirmedRequests;

    @JsonSerialize(using = CustomLocalDateTimeSerializer.class)
    @JsonDeserialize(using = CustomLocalDateTimeDeserializer.class)
    private LocalDateTime eventDate;
    private UserDto initiator;
    private Boolean paid;
    private String title;
    private Long views;
}
