package teamfive.category.service;

import teamfive.category.dto.InputCategoryDto;
import teamfive.category.dto.OutputCategoryDto;
import teamfive.category.dto.UpdateCategoryDto;

import java.util.List;

public interface CategoryService {
    OutputCategoryDto createCategory(InputCategoryDto inputCategoryDto);

    void delete(Long categoryId);

    OutputCategoryDto updateCategory(UpdateCategoryDto updateCategoryDto);

    OutputCategoryDto getById(Long categoryId);

    List<OutputCategoryDto> getAll(int from, int size);
}
