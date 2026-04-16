package ru.practicum.dto.event;

import lombok.Data;
import ru.practicum.enums.EventState;

@Data
public class EventForRequestDto {
    private Long id;
    private EventState state;
    private Long initiatorId;
    private Integer participantLimit;
    private Boolean requestModeration;
}
