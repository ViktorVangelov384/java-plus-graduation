package ru.yandex.practicum.compilation.model;

import jakarta.persistence.*;
import lombok.*;
import ru.yandex.practicum.event.model.Event;

import java.util.Set;

@Entity
@Table(name = "compilations",
        uniqueConstraints = {
                @UniqueConstraint(name = "uq_compilation_title", columnNames = "title")
        })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Compilation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToMany
    @JoinTable(
            name = "compilation_events",
            joinColumns = @JoinColumn(name = "compilation_id"),
            inverseJoinColumns = @JoinColumn(name = "event_id")
    )
    private Set<Event> events;

    @Column(nullable = false)
    private Boolean pinned = false;

    @Column(nullable = false, unique = true, length = 50)
    private String title;
}