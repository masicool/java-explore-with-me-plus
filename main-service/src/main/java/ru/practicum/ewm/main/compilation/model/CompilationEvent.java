package ru.practicum.ewm.main.compilation.model;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;
import ru.practicum.ewm.main.event.model.Event;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@FieldDefaults(level = AccessLevel.PRIVATE)
@Table(name = "events_compilations")
public class CompilationEvent  {
    @EmbeddedId
    CompilationEventKey id;

    @ManyToOne
    @MapsId("eventId")
    @JoinColumn(name = "event_id")
    Event event;

    @ManyToOne
    @MapsId("compilationId")
    @JoinColumn(name = "compilation_id")
    Compilation compilation;

    public CompilationEvent(Compilation compilation, Event event) {
        this.compilation = compilation;
        this.event = event;
    }
}
