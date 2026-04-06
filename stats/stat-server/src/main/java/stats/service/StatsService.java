package stats.service;

import dto.StatDto;

import java.time.LocalDateTime;
import java.util.List;

public interface StatsService {

    StatDto saveRequest(StatDto statDto);

    List<StatDto> getStats(LocalDateTime start,
                           LocalDateTime end,
                           List<String> uris,
                           boolean unique);
}
