package teamfive.compilation.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import teamfive.compilation.dto.CompilationRequestDto;
import teamfive.compilation.dto.CompilationResponseDto;
import teamfive.compilation.dto.CompilationUpdateRequestDto;
import teamfive.compilation.mapper.CompilationMapper;
import teamfive.compilation.model.Compilation;
import teamfive.compilation.storage.CompilationRepository;
import teamfive.event.model.Event;
import teamfive.event.storage.EventRepository;
import teamfive.exception.DuplicatedException;
import teamfive.exception.NotFoundException;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CompilationServiceImpl implements CompilationService {

    private final CompilationRepository compilationRepository;
    private final EventRepository eventRepository;
    private final CompilationMapper compilationMapper;

    @Transactional
    @Override
    public CompilationResponseDto createCompilation(CompilationRequestDto request) {
        log.info("Создание подборки: title={}, events={}", request.getTitle(), request.getEvents());

        if (request.getTitle() == null || request.getTitle().trim().isEmpty()) {
            throw new IllegalArgumentException("Title cannot be null or empty");
        }

        if (compilationRepository.existsByTitle(request.getTitle())) {
            throw new DuplicatedException("Подборка с названием '" + request.getTitle() + "' уже существует");
        }

        Set<Event> events = new HashSet<>();
        if (request.getEvents() != null && !request.getEvents().isEmpty()) {
            events = new HashSet<>(eventRepository.findByIdIn(
                    request.getEvents().stream().collect(Collectors.toList())
            ));
            if (events.size() != request.getEvents().size()) {
                log.warn("Some events not found. Requested: {}, Found: {}",
                        request.getEvents().size(), events.size());
            }
        }

        Compilation compilation = Compilation.builder()
                .events(events)
                .pinned(request.getPinned() != null ? request.getPinned() : false)
                .title(request.getTitle())
                .build();

        Compilation savedCompilation = compilationRepository.save(compilation);
        log.info("Подборка создана: id={}", savedCompilation.getId());

        return compilationMapper.toCompilationResponseDto(savedCompilation);
    }

    @Transactional
    @Override
    public void deleteCompilation(Long compId) {
        log.info("Удаление подборки: id={}", compId);

        if (!compilationRepository.existsById(compId)) {
            throw new NotFoundException("Подборка с id=" + compId + " не найдена");
        }

        compilationRepository.deleteById(compId);
        log.info("Подборка удалена: id={}", compId);
    }

    @Transactional
    @Override
    public CompilationResponseDto updateCompilation(Long compId, CompilationUpdateRequestDto request) {
        log.info("Обновление подборки: id={}", compId);

        Compilation compilation = compilationRepository.findById(compId)
                .orElseThrow(() -> new NotFoundException("Подборка с id=" + compId + " не найдена"));

        if (request.getTitle() != null &&
                !request.getTitle().equals(compilation.getTitle()) &&
                compilationRepository.existsByTitle(request.getTitle())) {
            throw new DuplicatedException("Подборка с названием '" + request.getTitle() + "' уже существует");
        }

        if (request.getEvents() != null) {
            Set<Event> events = new HashSet<>(eventRepository.findByIdIn(
                    request.getEvents().stream().collect(Collectors.toList())
            ));
            compilation.setEvents(events);
        }

        if (request.getPinned() != null) {
            compilation.setPinned(request.getPinned());
        }
        if (request.getTitle() != null) {
            compilation.setTitle(request.getTitle());
        }

        Compilation updatedCompilation = compilationRepository.save(compilation);
        log.info("Подборка обновлена: id={}", compId);

        return compilationMapper.toCompilationResponseDto(updatedCompilation);
    }

    @Override
    public List<CompilationResponseDto> getCompilations(Boolean pinned, int from, int size) {
        log.info("Получение подборок: pinned={}, from={}, size={}", pinned, from, size);

        validatePaginationParams(from, size);
        int page = calculatePageNumber(from, size);

        Pageable pageable = PageRequest.of(page, size, Sort.by("id").ascending());

        Page<Compilation> compilations;
        if (pinned != null) {
            compilations = compilationRepository.findByPinned(pinned, pageable);
        } else {
            compilations = compilationRepository.findAll(pageable);
        }

        return compilations.getContent().stream()
                .map(compilationMapper::toCompilationResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    public CompilationResponseDto getCompilationById(Long compId) {
        log.info("Получение подборки по id: {}", compId);

        Compilation compilation = compilationRepository.findByIdWithEvents(compId)
                .orElseThrow(() -> new NotFoundException("Подборка с id=" + compId + " не найдена"));

        return compilationMapper.toCompilationResponseDto(compilation);
    }

    private void validatePaginationParams(int from, int size) {
        if (size <= 0) {
            throw new IllegalArgumentException("Size must be positive");
        }
        if (from < 0) {
            throw new IllegalArgumentException("From must be non-negative");
        }
    }

    private int calculatePageNumber(int from, int size) {
        return from / size;
    }
}