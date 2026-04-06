package stats.mapper;

import dto.StatDto;
import org.mapstruct.Mapper;
import stats.model.Stats;

@Mapper(componentModel = "spring")
public interface StatsMapper {

    StatDto toDto(Stats stats);

    Stats toEntity(StatDto statDto);
}
