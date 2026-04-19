package ru.practicum.event.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class EventUpdateRequestDto {
    @Size(min = 20, max = 2000)
    private String annotation;

    private Long category;

    @Size(min = 20, max = 7000)
    private String description;

    @Future
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime eventDate;

    private EventLocationDto location;
    private Boolean paid;

    @PositiveOrZero
    private Integer participantLimit;
    private Boolean requestModeration;

    @Pattern(regexp = "PUBLISH_EVENT|REJECT_EVENT|SEND_TO_REVIEW|CANCEL_REVIEW",
            message = "stateAction must be one of: PUBLISH_EVENT, REJECT_EVENT, SEND_TO_REVIEW, CANCEL_REVIEW")
    private String stateAction;

    @Size(min = 3, max = 120)
    private String title;
}
