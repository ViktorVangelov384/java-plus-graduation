package teamfive.compilation.service;

import teamfive.compilation.dto.CompilationRequestDto;
import teamfive.compilation.dto.CompilationResponseDto;
import teamfive.compilation.dto.CompilationUpdateRequestDto;

import java.util.List;

public interface CompilationService {

    CompilationResponseDto createCompilation(CompilationRequestDto request);

    void deleteCompilation(Long compId);

    CompilationResponseDto updateCompilation(Long compId, CompilationUpdateRequestDto request);

    List<CompilationResponseDto> getCompilations(Boolean pinned, int from, int size);

    CompilationResponseDto getCompilationById(Long compId);
}