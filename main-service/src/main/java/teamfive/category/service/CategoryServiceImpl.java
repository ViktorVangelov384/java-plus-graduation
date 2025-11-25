package teamfive.category.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import teamfive.category.dto.InputCategoryDto;
import teamfive.category.dto.OutputCategoryDto;
import teamfive.category.dto.UpdateCategoryDto;
import teamfive.category.mapper.SimpleCategoryMapper;
import teamfive.category.model.Category;
import teamfive.category.storage.CategoryRepository;
import teamfive.exception.ConflictException;
import teamfive.exception.DuplicatedException;
import teamfive.exception.NotFoundException;
import teamfive.event.storage.EventRepository;

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
    public OutputCategoryDto createCategory(InputCategoryDto inputCategoryDto) {
        if (inputCategoryDto.getName() == null || inputCategoryDto.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Category name cannot be blank");
        }

        Category category = mapper.inputDtoToCategory(inputCategoryDto);
        log.info("Создаю category: name = {}", category.getName());
        try {
            return mapper.categoryToOutDto(repository.save(category));
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
    public OutputCategoryDto updateCategory(UpdateCategoryDto updateCategoryDto) {
        Category oldCategory = getCategoryById(updateCategoryDto.getId());
        Category newCategory = mapper.updateDtoToCategory(updateCategoryDto);
        try {
            return mapper.categoryToOutDto(repository.saveAndFlush(newCategory));
        } catch (DataIntegrityViolationException e) {
            throw new DuplicatedException(String.format("Category with name=%s already exists",
                    newCategory.getName()));
        }
    }

    @Override
    public OutputCategoryDto getById(Long categoryId) {
        return mapper.categoryToOutDto(getCategoryById(categoryId));
    }

    @Override
    public List<OutputCategoryDto> getAll(int from, int size) {

        validatePaginationParams(from, size);
        int page = calculatePageNumber(from, size);

        Pageable pageable = PageRequest.of(page, size, Sort.by("id").ascending());
        Page<Category> categories = repository.findAll(pageable);
        return categories.getContent()
                .stream()
                .map(mapper::categoryToOutDto)
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
