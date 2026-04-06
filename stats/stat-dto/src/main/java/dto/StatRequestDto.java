package dto;

import lombok.Data;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Data
public class StatRequestDto {
    private LocalDateTime start;
    private LocalDateTime end;
    private List<String> uris;
    private Boolean unique = false;

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public StatRequestDto() {
        LocalDateTime now = LocalDateTime.now();
        this.start = now.withHour(0).withMinute(0).withSecond(0).withNano(0);
        this.end = now.withHour(23).withMinute(59).withSecond(59).withNano(999999999);
        this.unique = false;
    }

    public StatRequestDto(LocalDateTime start, LocalDateTime end, List<String> uris, Boolean unique) {
        this.start = start != null ? start : LocalDateTime.now().withHour(0).withMinute(0).withSecond(0);
        this.end = end != null ? end : LocalDateTime.now().withHour(23).withMinute(59).withSecond(59);
        this.uris = uris;
        this.unique = unique != null ? unique : false;
    }

    public String getStartAsString() {
        return start.format(FORMATTER);
    }

    public String getEndAsString() {
        return end.format(FORMATTER);
    }
}