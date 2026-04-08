package ru.yandex.practicum.category.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.category.dto.CategoryDto;
import ru.yandex.practicum.category.dto.InputCategoryDto;
import ru.yandex.practicum.category.dto.UpdateCategoryDto;
import ru.yandex.practicum.category.mapper.SimpleCategoryMapper;
import ru.yandex.practicum.category.model.Category;
import ru.yandex.practicum.category.storage.CategoryRepository;
import ru.yandex.practicum.exception.ConflictException;
import ru.yandex.practicum.exception.DuplicatedException;
import ru.yandex.practicum.exception.NotFoundException;
import ru.yandex.practicum.event.storage.EventRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CategoryServiceImpl implements CategoryService {

    private final SimpleCategoryMapper mapper;
    private final CategoryRepository repository;
    private final EventRepository eventRepository;

    @Transactional
    @Override
    public CategoryDto createCategory(InputCategoryDto inputCategoryDto) {

        Category category = mapper.inputDtoToCategory(inputCategoryDto);
        log.info("Создаю category: name = {}", category.getName());
        try {
            return mapper.categoryToDto(repository.save(category));
        } catch (DataIntegrityViolationException e) {
            throw new DuplicatedException(
                    "Категория под именем  '" + inputCategoryDto.getName() + "' уже существует!"
            );
        }
    }

    @Transactional
    @Override
    public void delete(Long categoryId) {
        Category category = getCategoryById(categoryId);

        if (eventRepository.existsByCategoryId(categoryId)) {
            throw new ConflictException("Невозможно удалить категорию: существуют связанные события");
        }

        log.info("Удаляю категорию: id = {}, name = {}", categoryId, category.getName());
        repository.deleteById(categoryId);
    }

    @Transactional
    @Override
    public CategoryDto updateCategory(UpdateCategoryDto updateCategoryDto) {
        Category oldCategory = getCategoryById(updateCategoryDto.getId());
        Category newCategory = mapper.updateDtoToCategory(updateCategoryDto);
        try {
            return mapper.categoryToDto(repository.saveAndFlush(newCategory));
        } catch (DataIntegrityViolationException e) {
            throw new DuplicatedException(String.format("Category with name=%s already exists",
                    newCategory.getName()));
        }
    }

    @Override
    public CategoryDto getById(Long categoryId) {
        return mapper.categoryToDto(getCategoryById(categoryId));
    }

    @Override
    public List<CategoryDto> getAll(int from, int size) {

        validatePaginationParams(from, size);
        int page = calculatePageNumber(from, size);

        Pageable pageable = PageRequest.of(page, size, Sort.by("id").ascending());
        Page<Category> categories = repository.findAll(pageable);
        return categories.getContent()
                .stream()
                .map(mapper::categoryToDto)
                .collect(Collectors.toList());
    }

    private Category getCategoryById(Long categoryId) {
        return repository.findById(categoryId).orElseThrow(() ->
                new NotFoundException(String.format("Category with id=%d was not found", categoryId)));
    }

    private int calculatePageNumber(int from, int size) {
        if (size == 0) throw new IllegalArgumentException("Size cannot be zero");
        return from / size;
    }

    private void validatePaginationParams(int from, int size) {
        if (size <= 0) {
            throw new IllegalArgumentException("Size must be positive");
        }
        if (from < 0) {
            throw new IllegalArgumentException("From must be non-negative");
        }
    }
}
