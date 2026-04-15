package ru.yandex.practicum.request.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ParticipationRequestDto {
    private Long id;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS")
    private LocalDateTime created;

    @NotNull(message = "Id события не может быть null")
    @Positive(message = "Id события должен быть больше 0")
    private Long event;

    @NotNull(message = "Id пользователя не может быть null")
    @Positive(message = "Id пользователя должен быть больше 0")
    private Long requester;

    @NotNull(message = "Status cannot be null")
    private String status;
}
