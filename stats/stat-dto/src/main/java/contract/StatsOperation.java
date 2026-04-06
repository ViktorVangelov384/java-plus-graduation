package contract;

import dto.StatDto;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDateTime;
import java.util.List;

public interface StatsOperation {

    @GetMapping("/stats")
    List<StatDto> getStats(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime start,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime end,
            @RequestParam(defaultValue = "", required = false) List<String> uris,
            @RequestParam(defaultValue = "false", required = false) boolean unique
    );

    @PostMapping("/hit")
    StatDto saveHitRequest(@RequestBody StatDto statDto);
}
