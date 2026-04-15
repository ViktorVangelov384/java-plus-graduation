package ru.practicum.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class StatsErrorResponse {
    private final String error;
    private final String message;
}