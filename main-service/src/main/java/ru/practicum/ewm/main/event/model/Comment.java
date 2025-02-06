package ru.practicum.ewm.main.event.model;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import ru.practicum.ewm.main.user.model.User;

import java.time.LocalDateTime;

@Entity
@Table(name = "comments")
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Comment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    long id; // уникальный идентификатор комментария

    String text; // текст комментария

    @ManyToOne
    @JoinColumn(name = "event_id", nullable = false)
    Event event; // событие, к которому относится комментарий

    @ManyToOne
    @JoinColumn(name = "author_id")
    User author; // автор комментария

    @CreationTimestamp
    LocalDateTime created; // дата создания комментария

    @UpdateTimestamp
    LocalDateTime lastUpdate; // дата создания комментария
}
