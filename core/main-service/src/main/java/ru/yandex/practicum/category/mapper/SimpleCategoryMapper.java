package ru.yandex.practicum.category.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.yandex.practicum.category.dto.CategoryDto;
import ru.yandex.practicum.category.dto.InputCategoryDto;
import ru.yandex.practicum.category.dto.UpdateCategoryDto;
import ru.yandex.practicum.category.model.Category;

@Mapper(componentModel = "spring")
public interface SimpleCategoryMapper {

    @Mapping(target = "id", ignore = true)
    Category inputDtoToCategory(InputCategoryDto inputCategoryDto);

    Category updateDtoToCategory(UpdateCategoryDto updateCategoryDto);

    CategoryDto categoryToDto(Category category);
}
