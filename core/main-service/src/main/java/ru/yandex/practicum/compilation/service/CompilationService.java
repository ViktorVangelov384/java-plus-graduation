package ru.yandex.practicum.compilation.service;

import ru.yandex.practicum.compilation.dto.CompilationRequestDto;
import ru.yandex.practicum.compilation.dto.CompilationResponseDto;
import ru.yandex.practicum.compilation.dto.CompilationUpdateRequestDto;

import java.util.List;

public interface CompilationService {

    CompilationResponseDto createCompilation(CompilationRequestDto request);

    void deleteCompilation(Long compId);

    CompilationResponseDto updateCompilation(Long compId, CompilationUpdateRequestDto request);

    List<CompilationResponseDto> getCompilations(Boolean pinned, int from, int size);

    CompilationResponseDto getCompilationById(Long compId);
}