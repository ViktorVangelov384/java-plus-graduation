package teamfive.category.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class InputCategoryDto {
    @NotBlank
    @Size(min = 1, max = 150)
    private String name;
}
