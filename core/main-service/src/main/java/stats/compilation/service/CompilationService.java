package stats.compilation.service;

import stats.compilation.dto.CompilationRequestDto;
import stats.compilation.dto.CompilationResponseDto;
import stats.compilation.dto.CompilationUpdateRequestDto;

import java.util.List;

public interface CompilationService {

    CompilationResponseDto createCompilation(CompilationRequestDto request);

    void deleteCompilation(Long compId);

    CompilationResponseDto updateCompilation(Long compId, CompilationUpdateRequestDto request);

    List<CompilationResponseDto> getCompilations(Boolean pinned, int from, int size);

    CompilationResponseDto getCompilationById(Long compId);
}