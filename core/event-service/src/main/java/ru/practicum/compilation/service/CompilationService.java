package ru.practicum.compilation.service;

import ru.practicum.compilation.dto.CompilationRequestDto;
import ru.practicum.compilation.dto.CompilationResponseDto;
import ru.practicum.compilation.dto.CompilationUpdateRequestDto;

import java.util.List;

public interface CompilationService {

    CompilationResponseDto createCompilation(CompilationRequestDto request);

    void deleteCompilation(Long compId);

    CompilationResponseDto updateCompilation(Long compId, CompilationUpdateRequestDto request);

    List<CompilationResponseDto> getCompilations(Boolean pinned, int from, int size);

    CompilationResponseDto getCompilationById(Long compId);
}