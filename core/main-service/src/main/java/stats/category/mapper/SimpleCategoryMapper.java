package stats.category.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import stats.category.dto.CategoryDto;
import stats.category.dto.InputCategoryDto;
import stats.category.dto.UpdateCategoryDto;
import stats.category.model.Category;

@Mapper(componentModel = "spring")
public interface SimpleCategoryMapper {

    @Mapping(target = "id", ignore = true)
    Category inputDtoToCategory(InputCategoryDto inputCategoryDto);

    Category updateDtoToCategory(UpdateCategoryDto updateCategoryDto);

    CategoryDto categoryToDto(Category category);
}
