package client;

import dto.StatDto;
import dto.StatsResponseDto;

import java.time.LocalDateTime;
import java.util.List;

public interface StatsClientService {
    List<StatDto> getStats(LocalDateTime start,
                                    LocalDateTime end,
                                    List<String> uris,
                                    boolean unique);

    StatDto hit(StatDto statDto);
}