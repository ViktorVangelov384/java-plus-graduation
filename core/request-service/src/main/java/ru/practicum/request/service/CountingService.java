package ru.practicum.request.service;

import java.util.List;
import java.util.Map;

public interface CountingService {
    Map<Long, Integer> getCountConfirmedRequestsByEventIds(List<Long> eventsIds);
    int getCountConfirmedRequestsByEventId(Long eventId);
}