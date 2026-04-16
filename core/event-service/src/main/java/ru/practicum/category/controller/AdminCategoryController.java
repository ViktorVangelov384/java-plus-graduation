package ru.practicum.category.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.practicum.dto.category.CategoryDto;
import ru.practicum.category.dto.InputCategoryDto;
import ru.practicum.category.dto.UpdateCategoryDto;
import ru.practicum.category.service.CategoryService;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/admin")
public class AdminCategoryController {
    private final CategoryService categoryService;

    @PostMapping("/categories")
    public ResponseEntity<CategoryDto> create(@Valid @RequestBody InputCategoryDto inputCategoryDto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(categoryService.createCategory(inputCategoryDto));
    }

    @DeleteMapping("/categories/{catId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable("catId") Long categoryId) {
        categoryService.delete(categoryId);
    }

    @PatchMapping("/categories/{catId}")
    public ResponseEntity<CategoryDto> update(@PathVariable("catId") Long categoryId,
                                                    @Valid @RequestBody UpdateCategoryDto updateCategoryDto) {
        updateCategoryDto.setId(categoryId);
        return ResponseEntity.status(HttpStatus.OK)
                .body(categoryService.updateCategory(updateCategoryDto));
    }
}
