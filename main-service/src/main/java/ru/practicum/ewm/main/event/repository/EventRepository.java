package ru.practicum.ewm.main.event.repository;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import ru.practicum.ewm.main.event.model.Event;

import java.util.List;

public interface EventRepository extends JpaRepository<Event, Long>, QuerydslPredicateExecutor<Event> {
    List<Event> findAllByInitiatorId(long userId, PageRequest page);

    @Query(value = "select e.* " +
            "from events e " +
            "join events_compilations ec on ec.event_id = e.id " +
            "where ec.compilation_id = :compilationId", nativeQuery = true)
    List<Event> getEventsByCompilationId(Long compilationId);

    List<Event> findByIdIn(List<Long> events);
}
