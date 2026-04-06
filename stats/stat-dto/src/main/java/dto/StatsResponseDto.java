package dto;


import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class StatsResponseDto {
    String app;
    String uri;
    Long hits;
}
