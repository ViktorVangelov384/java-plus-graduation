package ru.practicum.category.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.dto.category.CategoryDto;
import ru.practicum.category.dto.InputCategoryDto;
import ru.practicum.category.dto.UpdateCategoryDto;
import ru.practicum.event.exception.DuplicatedException;
import ru.practicum.category.mapper.SimpleCategoryMapper;
import ru.practicum.category.model.Category;
import ru.practicum.category.storage.CategoryRepository;
import ru.practicum.event.exception.ConflictException;
import ru.practicum.event.exception.NotFoundException;
import ru.practicum.event.storage.EventRepository;

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
        Category existingCategory = getCategoryById(updateCategoryDto.getId());

        if (updateCategoryDto.getName() != null) {
            if (!existingCategory.getName().equals(updateCategoryDto.getName()) &&
                    repository.existsByName(updateCategoryDto.getName())) {
                throw new DuplicatedException("Category with name=" + updateCategoryDto.getName() + " already exists");
            }
            existingCategory.setName(updateCategoryDto.getName());
        }

        return mapper.categoryToDto(repository.save(existingCategory));
    }

    @Override
    public CategoryDto getById(Long categoryId) {
        return mapper.categoryToDto(getCategoryById(categoryId));
    }

    @Override
    public List<CategoryDto> getAll(int from, int size) {
        validatePaginationParams(from, size);
        Pageable pageable = PageRequest.of(from, size, Sort.by("id").ascending());
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

    private void validatePaginationParams(int from, int size) {
        if (size <= 0) {
            throw new IllegalArgumentException("Size must be positive");
        }
        if (from < 0) {
            throw new IllegalArgumentException("From must be non-negative");
        }
    }
}
