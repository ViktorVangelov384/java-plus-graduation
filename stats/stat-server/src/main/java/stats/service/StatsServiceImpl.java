package stats.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.dto.StatDto;
import ru.practicum.dto.StatsResponseDto;
import stats.mapper.StatsMapper;
import stats.model.Stats;
import stats.storage.StatRepository;

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

        if (end.isBefore(start)) {
            throw new DateTimeException("Дата окончания не может быть раньше даты начала");
        }

        List<String> uriList = (uris == null || uris.isEmpty()) ? null : uris;

        if (unique) {
            return statRepository.getUniqueStat(start, end, uriList);
        } else {
            return statRepository.getNonUniqueStat(start, end, uriList);
        }

    }
}