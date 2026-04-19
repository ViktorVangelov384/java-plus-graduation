package ru.practicum.category.service;

import ru.practicum.dto.category.CategoryDto;
import ru.practicum.category.dto.InputCategoryDto;
import ru.practicum.category.dto.UpdateCategoryDto;

import java.util.List;

public interface CategoryService {
    CategoryDto createCategory(InputCategoryDto inputCategoryDto);

    void delete(Long categoryId);

    CategoryDto updateCategory(UpdateCategoryDto updateCategoryDto);

    CategoryDto getById(Long categoryId);

    List<CategoryDto> getAll(int from, int size);
}
