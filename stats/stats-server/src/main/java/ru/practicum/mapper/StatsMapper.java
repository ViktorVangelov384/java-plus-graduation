package ru.practicum.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import ru.yandex.practicum.dto.StatDto;
import ru.practicum.model.Stats;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface StatsMapper {

    StatDto toDto(Stats stats);

    Stats toEntity(StatDto statDto);
}
