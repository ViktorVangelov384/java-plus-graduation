package ru.practicum.user.exception;

public class DataAlreadyInUseException extends RuntimeException {
    public DataAlreadyInUseException(String message) {
        super(message);
    }
}
