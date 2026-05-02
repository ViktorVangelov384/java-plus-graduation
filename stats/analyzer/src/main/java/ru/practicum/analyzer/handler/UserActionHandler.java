package ru.practicum.analyzer.handler;

import ru.practicum.stats.avro.UserActionAvro;

public interface UserActionHandler {
    void handle(UserActionAvro action);
}