package ru.practicum.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class StatsErrorHandler {

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public StatsErrorResponse handleIllegalArgumentException(IllegalArgumentException e) {
        log.error("Bad request: {}", e.getMessage());
        return new StatsErrorResponse("Bad Request", e.getMessage());
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public StatsErrorResponse handleException(Exception e) {
        log.error("Internal error: ", e);
        return new StatsErrorResponse("Internal Server Error", "An unexpected error occurred");
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public StatsErrorResponse handleMissingParameter(MissingServletRequestParameterException e) {
        log.error("Missing required parameter: {}", e.getParameterName());
        return new StatsErrorResponse("Bad Request", "Required request parameter '" + e.getParameterName() + "' is not present");
    }
}