package client;

import dto.StatDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StatsClientServiceImpl implements StatsClientService {

    private final StatsClient statsClient;

    @Override
    public List<StatDto> getStats(LocalDateTime start, LocalDateTime end, List<String> uris, boolean unique) {
        return statsClient.getStats(start, end, uris, unique);

    }

    @Override
    public StatDto hit(StatDto statDto) {
        return statsClient.saveHitRequest(statDto);
    }
}
