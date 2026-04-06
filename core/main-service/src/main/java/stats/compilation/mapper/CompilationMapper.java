package stats.compilation.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import stats.compilation.dto.CompilationResponseDto;
import stats.compilation.model.Compilation;

@Mapper(componentModel = "spring")
public interface CompilationMapper {

    @Mapping(target = "events", source = "events")
    CompilationResponseDto toCompilationResponseDto(Compilation compilation);
}