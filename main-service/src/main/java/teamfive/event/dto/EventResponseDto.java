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
public class EventResponseDto {

    private Long id;
    private String annotation;
    private OutputCategoryDto category;
    private Integer confirmedRequests;
    private LocalDateTime createdOn;
    private String description;

    @JsonSerialize(using = CustomLocalDateTimeSerializer.class)
    @JsonDeserialize(using = CustomLocalDateTimeDeserializer.class)
    private LocalDateTime eventDate;
    private UserDto initiator;
    private EventLocationDto location;
    private Boolean paid;
    private Integer participantLimit;
    private LocalDateTime publishedOn;
    private Boolean requestModeration;
    private String state;
    private String title;
    private Long views;
}
