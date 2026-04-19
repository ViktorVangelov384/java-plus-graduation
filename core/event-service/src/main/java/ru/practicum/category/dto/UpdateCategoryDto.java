package ru.practicum.category.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateCategoryDto {
    private Long id;

    @NotBlank
    @Size(min = 1, max = 50)
    private String name;
}
