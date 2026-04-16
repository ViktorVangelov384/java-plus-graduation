package ru.practicum.category.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.practicum.dto.category.CategoryDto;
import ru.practicum.category.dto.InputCategoryDto;
import ru.practicum.category.dto.UpdateCategoryDto;
import ru.practicum.category.model.Category;

@Mapper(componentModel = "spring")
public interface SimpleCategoryMapper {

    @Mapping(target = "id", ignore = true)
    Category inputDtoToCategory(InputCategoryDto inputCategoryDto);

    Category updateDtoToCategory(UpdateCategoryDto updateCategoryDto);

    CategoryDto categoryToDto(Category category);
}
