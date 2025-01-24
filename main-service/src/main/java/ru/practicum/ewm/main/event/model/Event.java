package ru.practicum.ewm.main.event.model;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;
import ru.practicum.ewm.main.category.model.Category;
import ru.practicum.ewm.main.user.model.User;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@Entity
@Table(name = "events")
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Event {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;
    String annotation;

    @ManyToOne
    @JoinColumn(name = "category_id")
    Category category;

    String description;

    @Column(name = "event_date")
    LocalDateTime eventDate;

    Float lat;
    Float lon;

    Boolean paid;

    @Column(name = "participant_limit")
    Integer participantLimit;

    @Column(name = "request_moderation")
    Boolean requestModeration;

    String title;

    LocalDateTime created;

    @ManyToOne
    @JoinColumn(name = "initiator_id")
    User initiator;

    @Enumerated(EnumType.STRING)
    State state;

    LocalDateTime published;
}
