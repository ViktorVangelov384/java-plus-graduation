package ru.practicum.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.dto.StatDto;
import ru.yandex.practicum.dto.StatsResponseDto;
import ru.practicum.mapper.StatsMapper;
import ru.practicum.model.Stats;
import ru.practicum.storage.StatRepository;

import java.time.DateTimeException;
import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StatsServiceImpl implements StatsService {

    private final StatRepository statRepository;
    private final StatsMapper statsMapper;

    @Override
    @Transactional
    public StatDto saveRequest(StatDto statDto) {
       if (statDto == null) {
            throw new IllegalArgumentException("StatDto не может быть null");
        }

        Stats stat = statsMapper.toEntity(statDto);
        Stats savedStat = statRepository.save(stat);
        return statsMapper.toDto(savedStat);
    }

    @Override
    public List<StatsResponseDto> getStats(LocalDateTime start,
                                           LocalDateTime end,
                                           List<String> uris,
                                           boolean unique) {
        if (start == null) {
            start = LocalDateTime.now().minusDays(30);
        }
        if (end == null) {
            end = LocalDateTime.now();
        }

        if (end.isBefore(start)) {
            throw new DateTimeException("Дата окончания не может быть раньше даты начала");
        }

        if (uris == null || uris.isEmpty()) {
            if (unique) {
                return statRepository.getStatsWithoutUriWithUniqueIp(start, end);
            } else {
                return statRepository.getStatsWithoutUri(start, end);
            }
        } else {
            if (unique) {
                return statRepository.getStatsWithUriWithUniqueIp(start, end, uris);
            } else {
                return statRepository.getStatsWithUri(start, end, uris);
            }
        }
    }
}