package ru.practicum.request.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.enums.RequestStatus;
import ru.practicum.request.model.Request;

import java.util.List;
import java.util.Optional;

public interface RequestRepository extends JpaRepository<Request, Long> {

    List<Request> findAllByRequesterId(Long requesterId);

    List<Request> findAllByEventId(Long eventId);

    @Query("SELECT COUNT(r) FROM Request r WHERE r.eventId = :eventId AND r.status = 'CONFIRMED'")
    int findCountOfConfirmedRequestsByEventId(@Param("eventId") Long eventId);

    @Query("SELECT r.eventId as eventId, COUNT(r) as count " +
            "FROM Request r " +
            "WHERE r.eventId IN :eventsIds AND r.status = 'CONFIRMED' " +
            "GROUP BY r.eventId")
    List<EventRequestCount> findCountConfirmedByEventIds(@Param("eventsIds") List<Long> eventsIds);

    @Modifying
    @Transactional
    @Query("UPDATE Request r SET r.status = :status WHERE r.id IN :ids")
    void updateStatus(@Param("status") RequestStatus status, @Param("ids") List<Long> ids);

    boolean existsByEventIdAndRequesterId(long eventId, long requesterId);

    interface EventRequestCount {
        Long getEventId();
        Integer getCount();
    }
}
