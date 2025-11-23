package teamfive.category.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import teamfive.category.dto.InputCategoryDto;
import teamfive.category.dto.OutputCategoryDto;
import teamfive.category.dto.UpdateCategoryDto;
import teamfive.category.model.Category;

@Mapper(componentModel = "spring")
public interface SimpleCategoryMapper {

    @Mapping(target = "id", ignore = true)
    Category inputDtoToCategory(InputCategoryDto inputCategoryDto);

    @Mapping(target = "id", ignore = true)
    Category updateDtoToCategory(UpdateCategoryDto updateCategoryDto);

    OutputCategoryDto categoryToOutDto(Category category);
}
