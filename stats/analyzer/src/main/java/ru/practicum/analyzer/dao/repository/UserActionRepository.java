package ru.practicum.analyzer.dao.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.analyzer.dao.model.UserAction;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface UserActionRepository extends JpaRepository<UserAction, Long> {

    Optional<UserAction> findByUserIdAndEventId(long userId, long eventId);

    @Query("select a.eventId from UserAction a where a.userId = :userId and a.eventId != :eventId")
    Set<Long> findEventIdsByUserIdExcludeEventId(@Param("userId") long userId, @Param("eventId") long eventId);

    @Query("select a.eventId from UserAction a where a.userId = :userId order by a.actionTimestamp desc")
    List<Long> findEventIdsByUserId(@Param("userId") long userId, Pageable pageable);

    @Query("select a.eventId, sum(a.weight) from UserAction a where a.eventId in :eventIds group by a.eventId")
    List<Object[]> sumWeightsByEventId(@Param("eventIds") Set<Long> eventIds);

    List<UserAction> findAllByUserId(long userId);
}
