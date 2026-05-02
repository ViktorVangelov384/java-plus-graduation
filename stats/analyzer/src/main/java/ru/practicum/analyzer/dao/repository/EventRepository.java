package ru.practicum.analyzer.dao.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.analyzer.dao.model.Event;

public interface EventRepository extends JpaRepository<Event, Long> {
}
