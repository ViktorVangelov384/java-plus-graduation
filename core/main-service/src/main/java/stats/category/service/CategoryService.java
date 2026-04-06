package stats.category.service;

import stats.category.dto.CategoryDto;
import stats.category.dto.InputCategoryDto;
import stats.category.dto.UpdateCategoryDto;

import java.util.List;

public interface CategoryService {
    CategoryDto createCategory(InputCategoryDto inputCategoryDto);

    void delete(Long categoryId);

    CategoryDto updateCategory(UpdateCategoryDto updateCategoryDto);

    CategoryDto getById(Long categoryId);

    List<CategoryDto> getAll(int from, int size);
}
