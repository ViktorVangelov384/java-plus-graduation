package ru.practicum.analyzer.handler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.practicum.analyzer.dao.model.EventSimilarity;
import ru.practicum.analyzer.dao.repository.EventSimilarityRepository;
import ru.practicum.stats.avro.EventSimilarityAvro;

import java.util.Optional;

@Component
@RequiredArgsConstructor
@Slf4j
public class SimilarityHandlerImpl implements SimilarityHandler {

    private final EventSimilarityRepository repository;

    @Override
    public void handle(EventSimilarityAvro similarity) {
        Optional<EventSimilarity> existing = repository.findByEventAAndEventB(
                similarity.getEventA(), similarity.getEventB());

        EventSimilarity entity = existing.orElseGet(() -> EventSimilarity.builder()
                .eventA(similarity.getEventA())
                .eventB(similarity.getEventB())
                .build());

        entity.setScore(similarity.getScore());
        repository.save(entity);
    }
}
