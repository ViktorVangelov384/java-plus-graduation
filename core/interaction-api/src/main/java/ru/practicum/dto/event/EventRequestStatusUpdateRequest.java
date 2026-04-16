package ru.practicum.dto.event;

import lombok.Data;
import ru.practicum.enums.RequestStatus;

import java.util.List;

@Data
public class EventRequestStatusUpdateRequest {
    private List<Long> requestIds;
    private RequestStatus status;
}
