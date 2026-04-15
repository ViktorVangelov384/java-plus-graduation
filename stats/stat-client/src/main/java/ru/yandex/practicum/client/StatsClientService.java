package ru.yandex.practicum.client;

import ru.yandex.practicum.dto.StatDto;
import ru.yandex.practicum.dto.StatsResponseDto;

import java.time.LocalDateTime;
import java.util.List;

public interface StatsClientService {
    List<StatsResponseDto> getStats(LocalDateTime start,
                                    LocalDateTime end,
                                    List<String> uris,
                                    boolean unique);

    StatDto hit(StatDto statDto);
}