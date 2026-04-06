package stats.controller;

import contract.StatsOperation;
import dto.StatDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import stats.service.StatsService;

import java.time.LocalDateTime;
import java.util.List;
@Slf4j
@RestController
@RequiredArgsConstructor
public class StatController implements StatsOperation {

    private final StatsService statsService;

    @GetMapping("/stats")
    @ResponseStatus(HttpStatus.OK)
    public List<StatDto> getStats(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime start,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime end,
            @RequestParam(defaultValue = "", required = false) List<String> uris,
            @RequestParam(defaultValue = "false", required = false) boolean unique) {

        log.debug("GET /stats: start={}, end={}, uris={}, unique={}", start, end, uris, unique);
        return statsService.getStats(start, end, uris, unique);
    }

    @PostMapping("/hit")
    @ResponseStatus(HttpStatus.CREATED)
    public StatDto saveHitRequest(@Valid @RequestBody StatDto statDto) {
        log.debug("POST /hit: app={}, uri={}, ip={}, timestamp={}",
                statDto.getApp(), statDto.getUri(), statDto.getIp(), statDto.getTimestamp());
        return statsService.saveRequest(statDto);
    }
}
