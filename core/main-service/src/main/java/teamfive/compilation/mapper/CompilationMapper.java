package teamfive.compilation.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import teamfive.compilation.dto.CompilationResponseDto;
import teamfive.compilation.model.Compilation;

@Mapper(componentModel = "spring")
public interface CompilationMapper {

    @Mapping(target = "events", source = "events")
    CompilationResponseDto toCompilationResponseDto(Compilation compilation);
}