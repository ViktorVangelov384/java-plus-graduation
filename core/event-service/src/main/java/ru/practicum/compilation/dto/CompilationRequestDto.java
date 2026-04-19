package ru.practicum.compilation.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.Set;

@Data
public class CompilationRequestDto {
    private Set<Long> events;

    private Boolean pinned = false;

    @NotBlank
    @Size(min = 1, max = 50)
    private String title;
}