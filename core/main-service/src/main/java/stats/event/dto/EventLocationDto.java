package stats.event.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class EventLocationDto {
    @NotNull
    private Double lat;

    @NotNull
    private Double lon;
}

