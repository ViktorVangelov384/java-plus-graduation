package ru.practicum.compilation.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.practicum.compilation.dto.CompilationResponseDto;
import ru.practicum.compilation.model.Compilation;

@Mapper(componentModel = "spring")
public interface CompilationMapper {

    @Mapping(target = "events", source = "events")
    CompilationResponseDto toCompilationResponseDto(Compilation compilation);
}