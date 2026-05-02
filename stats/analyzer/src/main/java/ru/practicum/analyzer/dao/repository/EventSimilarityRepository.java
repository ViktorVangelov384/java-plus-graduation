package ru.practicum.analyzer.dao.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.analyzer.dao.model.EventSimilarity;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface EventSimilarityRepository extends JpaRepository<EventSimilarity, Long> {

    Optional<EventSimilarity> findByEventAAndEventB(long eventA, long eventB);

    @Query("select s from EventSimilarity s where s.eventA = :eventId or s.eventB = :eventId")
    List<EventSimilarity> findAllByEventId(@Param("eventId") long eventId);

    @Query("select s from EventSimilarity s where (s.eventA IN :eventIds OR s.eventB IN :eventIds)")
    List<EventSimilarity> findByEventIdIn(@Param("eventIds") Set<Long> eventIds, Pageable pageable);
}

