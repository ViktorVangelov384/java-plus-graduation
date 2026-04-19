package ru.practicum.analyzer.handler;

import ru.practicum.stats.avro.EventSimilarityAvro;

public interface SimilarityHandler {
    void handle(EventSimilarityAvro similarity);
}
