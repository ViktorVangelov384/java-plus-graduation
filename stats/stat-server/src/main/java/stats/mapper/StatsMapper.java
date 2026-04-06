package stats.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import ru.practicum.dto.StatDto;
import stats.model.Stats;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface StatsMapper {

    StatDto toDto(Stats stats);

    Stats toEntity(StatDto statDto);
}
