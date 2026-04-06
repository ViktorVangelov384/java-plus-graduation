package stats.service;

import ru.practicum.dto.StatDto;
import ru.practicum.dto.StatsResponseDto;

import java.time.LocalDateTime;
import java.util.List;

public interface StatsService {

    StatDto saveRequest(StatDto statDto);

    List<StatsResponseDto> getStats(LocalDateTime start,
                                    LocalDateTime end,
                                    List<String> uris,
                                    boolean unique);
}
