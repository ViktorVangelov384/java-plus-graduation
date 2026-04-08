package ru.practicum.service;

import ru.yandex.practicum.dto.StatDto;
import ru.yandex.practicum.dto.StatsResponseDto;

import java.time.LocalDateTime;
import java.util.List;

public interface StatsService {

    StatDto saveRequest(StatDto statDto);

    List<StatsResponseDto> getStats(LocalDateTime start,
                                    LocalDateTime end,
                                    List<String> uris,
                                    boolean unique);
}
