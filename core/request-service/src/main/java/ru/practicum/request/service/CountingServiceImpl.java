package ru.practicum.request.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.request.repository.RequestRepository;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CountingServiceImpl implements CountingService {

    private final RequestRepository requestRepository;

    @Override
    public Map<Long, Integer> getCountConfirmedRequestsByEventIds(List<Long> eventsIds) {
        if (eventsIds == null || eventsIds.isEmpty()) {
            return Map.of();
        }
        Map<Long, Integer> result = requestRepository.findCountConfirmedByEventIds(eventsIds).stream()
                .collect(Collectors.toMap(
                        RequestRepository.EventRequestCount::getEventId,
                        RequestRepository.EventRequestCount::getCount
                ));

        for (Long eventId : eventsIds) {
            result.putIfAbsent(eventId, 0);
        }

        return result;
    }

    @Override
    public int getCountConfirmedRequestsByEventId(Long eventId) {
        return requestRepository.findCountOfConfirmedRequestsByEventId(eventId);
    }
}
