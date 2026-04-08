package ru.yandex.practicum.event.dto;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import jakarta.validation.constraints.*;
import lombok.Data;import ru.yandex.practicum.category.configuration.CustomLocalDateTimeDeserializer;
import ru.yandex.practicum.category.configuration.CustomLocalDateTimeSerializer;
import ru.yandex.practicum.category.dto.CategoryDto;
import ru.yandex.practicum.user.dto.UserDto;

import java.time.LocalDateTime;

@Data
public class EventResponseDto {

    @Positive(message = "ID должен быть положительным числом")
    private Long id;

    @NotBlank(message = "Аннотация не может быть пустой")
    @Size(min = 20, max = 2000, message = "Аннотация должна содержать от 20 до 2000 символов")
    private String annotation;

    @NotNull(message = "Категория не может быть null")
    private CategoryDto category;

    @PositiveOrZero(message = "Количество подтвержденных запросов не может быть отрицательным")
    private Integer confirmedRequests;

    @NotNull(message = "Дата создания не может быть null")
    private LocalDateTime createdOn;

    @NotBlank(message = "Описание не может быть пустым")
    @Size(min = 20, max = 7000, message = "Описание должно содержать от 20 до 7000 символов")
    private String description;

    @NotNull(message = "Дата события не может быть null")
    @Future(message = "Дата события должна быть в будущем")
    @JsonSerialize(using = CustomLocalDateTimeSerializer.class)
    @JsonDeserialize(using = CustomLocalDateTimeDeserializer.class)
    private LocalDateTime eventDate;

    @NotNull(message = "Инициатор не может быть null")
    private UserDto initiator;

    @NotNull(message = "Локация не может быть null")
    private EventLocationDto location;
    private Boolean paid;

    @PositiveOrZero(message = "Лимит участников не может быть отрицательным")
    private Integer participantLimit;
    private LocalDateTime publishedOn;
    private Boolean requestModeration;

    @NotBlank(message = "Статус не может быть пустым")
    private String state;
    private String title;

    @PositiveOrZero(message = "Количество просмотров не может быть отрицательным")
    private Long views;
}
