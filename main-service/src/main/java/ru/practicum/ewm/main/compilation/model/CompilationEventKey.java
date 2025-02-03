package ru.practicum.ewm.main.compilation.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public class CompilationEventKey {
    @Column(name = "event_id")
    private Long eventId;
    @Column(name = "compilation_id")
    private Long compilationId;
}
