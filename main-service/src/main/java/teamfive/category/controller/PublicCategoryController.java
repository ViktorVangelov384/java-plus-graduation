package teamfive.category.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import teamfive.category.dto.OutputCategoryDto;
import teamfive.category.service.CategoryService;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@RequestMapping("/categories")
public class PublicCategoryController {
    private final CategoryService categoryService;

    @GetMapping
    public List<OutputCategoryDto> getAll(@RequestParam(defaultValue = "0") int from,
                                          @RequestParam(defaultValue = "10") int size) {
        return categoryService.getAll(from, size);
    }

    @GetMapping("/{catId}")
    public OutputCategoryDto getById(@PathVariable("catId") Long categoryId) {
        return categoryService.getById(categoryId);
    }
}
