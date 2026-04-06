package stats.storage;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.dto.StatsResponseDto;
import stats.model.Stats;

import java.time.LocalDateTime;
import java.util.List;

public interface StatRepository extends JpaRepository<Stats, Long> {

    @Query("""
    SELECT new ru.practicum.dto.StatsResponseDto(s.app, s.uri, COUNT(DISTINCT s.ip))
    FROM Stats s
    WHERE s.timestamp BETWEEN :start AND :end
    AND (:uris IS NULL OR s.uri IN :uris)
    GROUP BY s.app, s.uri
    ORDER BY COUNT(DISTINCT s.ip) DESC
""")
    List<StatsResponseDto> getUniqueStat(@Param("start") LocalDateTime start,
                                         @Param("end") LocalDateTime end,
                                         @Param("uris") List<String> uris);


    @Query("""
    SELECT new ru.practicum.dto.StatsResponseDto(s.app, s.uri, COUNT(s.ip))
    FROM Stats s
    WHERE s.timestamp BETWEEN :start AND :end
    AND (:uris IS NULL OR s.uri IN :uris)
    GROUP BY s.app, s.uri
    ORDER BY COUNT(s.ip) DESC
""")
    List<StatsResponseDto> getNonUniqueStat(@Param("start") LocalDateTime start,
                                            @Param("end") LocalDateTime end,
                                            @Param("uris") List<String> uris);
}
