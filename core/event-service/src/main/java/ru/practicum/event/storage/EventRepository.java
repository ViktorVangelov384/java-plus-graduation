package ru.practicum.event.storage;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.enums.EventState;
import ru.practicum.event.model.Event;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface EventRepository extends JpaRepository<Event, Long>, JpaSpecificationExecutor<Event> {

    Page<Event> findByInitiatorId(Long initiatorId, Pageable pageable);

    Optional<Event> findByIdAndInitiatorId(Long eventId, Long initiatorId);

    List<Event> findByIdIn(List<Long> eventIds);

    @Query("SELECT e FROM Event e WHERE e.state = :state AND e.eventDate > :now")
    Page<Event> findPublishedEventsAfterDate(@Param("state") EventState state,
                                             @Param("now") LocalDateTime now,
                                             Pageable pageable);

    @Query("SELECT COUNT(e) > 0 FROM Event e WHERE e.category.id = :categoryId")
    boolean existsByCategoryId(@Param("categoryId") Long categoryId);

    @Modifying
    @Transactional
    @Query("UPDATE Event e SET e.confirmedRequests = :count WHERE e.id = :eventId")
    void updateConfirmedRequestsCount(@Param("eventId") Long eventId, @Param("count") Integer count);

    boolean existsByIdAndInitiatorId(Long eventId, Long userId);


}