package stats.compilation.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import stats.compilation.dto.CompilationRequestDto;
import stats.compilation.dto.CompilationResponseDto;
import stats.compilation.dto.CompilationUpdateRequestDto;
import stats.compilation.service.CompilationService;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/compilations")
public class AdminCompilationController {
    private final CompilationService compilationService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CompilationResponseDto createCompilation(@Valid @RequestBody CompilationRequestDto request) {
        log.info("POST /admin/compilations: title={}", request.getTitle());
        return compilationService.createCompilation(request);
    }

    @DeleteMapping("/{compId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCompilation(@PathVariable Long compId) {
        log.info("DELETE /admin/compilations/{}", compId);
        compilationService.deleteCompilation(compId);
    }

    @PatchMapping("/{compId}")
    public CompilationResponseDto updateCompilation(@PathVariable Long compId,
                                                    @Valid @RequestBody CompilationUpdateRequestDto request) {
        log.info("PATCH /admin/compilations/{}", compId);
        return compilationService.updateCompilation(compId, request);
    }
}