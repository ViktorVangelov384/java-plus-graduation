package ru.yandex.practicum.category.service;

import ru.yandex.practicum.category.dto.CategoryDto;
import ru.yandex.practicum.category.dto.InputCategoryDto;
import ru.yandex.practicum.category.dto.UpdateCategoryDto;

import java.util.List;

public interface CategoryService {
    CategoryDto createCategory(InputCategoryDto inputCategoryDto);

    void delete(Long categoryId);

    CategoryDto updateCategory(UpdateCategoryDto updateCategoryDto);

    CategoryDto getById(Long categoryId);

    List<CategoryDto> getAll(int from, int size);
}
