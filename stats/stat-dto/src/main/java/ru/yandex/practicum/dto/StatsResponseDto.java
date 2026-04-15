package ru.yandex.practicum.dto;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class StatsResponseDto {
    String app;
    String uri;
    Long hits;
}
