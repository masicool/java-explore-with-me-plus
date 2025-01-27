package ru.practicum.ewm.main.event.repository;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.ewm.main.event.model.Event;
import ru.practicum.ewm.main.event.model.State;

import java.time.LocalDateTime;
import java.util.List;

public interface EventRepository extends JpaRepository<Event, Long> {
    List<Event> findAllByInitiatorId(long userId, PageRequest page);

    List<Event> findByEventDateBefore(LocalDateTime rangeEnd, PageRequest page);

    List<Event> findByEventDateAfter(LocalDateTime rangeStart, PageRequest page);

    List<Event> findByEventDateAfterAndEventDateBefore(LocalDateTime rangeStart, LocalDateTime rangeEnd, PageRequest page);

    List<Event> findByCategoryIdIn(List<Long> categoryIds, PageRequest page);

    List<Event> findByCategoryIdInAndEventDateBefore(List<Long> categoryIds, LocalDateTime rangeEnd, PageRequest page);

    List<Event> findByCategoryIdInAndEventDateAfter(List<Long> categoryIds, LocalDateTime rangeStart, PageRequest page);

    List<Event> findByCategoryIdInAndEventDateAfterAndEventDateBefore(List<Long> categoryIds, LocalDateTime rangeStart, LocalDateTime rangeEnd, PageRequest page);

    List<Event> findByStateIn(List<State> states, PageRequest page);

    List<Event> findByStateInAndEventDateBefore(List<State> states, LocalDateTime rangeEnd, PageRequest page);

    List<Event> findByStateInAndEventDateAfter(List<State> states, LocalDateTime rangeStart, PageRequest page);

    List<Event> findByStateInAndEventDateAfterAndEventDateBefore(List<State> states, LocalDateTime rangeStart, LocalDateTime rangeEnd, PageRequest page);

    List<Event> findByStateInAndCategoryIdIn(List<State> states, List<Long> categoryIds, PageRequest page);

    List<Event> findByStateInAndCategoryIdInAndEventDateBefore(List<State> states, List<Long> categoryIds, LocalDateTime rangeEnd, PageRequest page);

    List<Event> findByStateInAndCategoryIdInAndEventDateAfter(List<State> states, List<Long> categoryIds, LocalDateTime rangeStart, PageRequest page);

    List<Event> findByStateInAndCategoryIdInAndEventDateAfterAndEventDateBefore(List<State> states, List<Long> categoryIds, LocalDateTime rangeStart, LocalDateTime rangeEnd, PageRequest page);

    List<Event> findByInitiatorIdIn(List<Long> userIds, PageRequest page);

    List<Event> findByInitiatorIdInAndEventDateBefore(List<Long> userIds, LocalDateTime rangeEnd, PageRequest page);

    List<Event> findByInitiatorIdInAndEventDateAfter(List<Long> userIds, LocalDateTime rangeStart, PageRequest page);

    List<Event> findByInitiatorIdInAndEventDateAfterAndEventDateBefore(List<Long> userIds, LocalDateTime rangeStart, LocalDateTime rangeEnd, PageRequest page);

    List<Event> findByInitiatorIdInAndCategoryIdIn(List<Long> userIds, List<Long> categoryIds, PageRequest page);

    List<Event> findByInitiatorIdInAndCategoryIdInAndEventDateBefore(List<Long> userIds, List<Long> categoryIds, LocalDateTime rangeEnd, PageRequest page);

    List<Event> findByInitiatorIdInAndCategoryIdInAndEventDateAfter(List<Long> userIds, List<Long> categoryIds, LocalDateTime rangeStart, PageRequest page);

    List<Event> findByInitiatorIdInAndCategoryIdInAndEventDateAfterAndEventDateBefore(List<Long> userIds, List<Long> categoryIds, LocalDateTime rangeStart, LocalDateTime rangeEnd, PageRequest page);

    List<Event> findByInitiatorIdInAndStateIn(List<Long> userIds, List<State> states, PageRequest page);

    List<Event> findByInitiatorIdInAndStateInAndEventDateBefore(List<Long> userIds, List<State> states, LocalDateTime rangeEnd, PageRequest page);

    List<Event> findByInitiatorIdInAndStateInAndEventDateAfter(List<Long> userIds, List<State> states, LocalDateTime rangeStart, PageRequest page);

    List<Event> findByInitiatorIdInAndStateInAndEventDateAfterAndEventDateBefore(List<Long> userIds, List<State> states, LocalDateTime rangeStart, LocalDateTime rangeEnd, PageRequest page);

    List<Event> findByInitiatorIdInAndStateInAndCategoryIdIn(List<Long> userIds, List<State> states, List<Long> categoryIds, PageRequest page);

    List<Event> findByInitiatorIdInAndStateInAndCategoryIdInAndEventDateBefore(List<Long> userIds, List<State> states, List<Long> categoryIds, LocalDateTime rangeEnd, PageRequest page);

    List<Event> findByInitiatorIdInAndStateInAndCategoryIdInAndEventDateAfter(List<Long> userIds, List<State> states, List<Long> categoryIds, LocalDateTime rangeStart, PageRequest page);

    List<Event> findByInitiatorIdInAndStateInAndCategoryIdInAndEventDateAfterAndEventDateBefore(List<Long> userIds, List<State> states, List<Long> categoryIds, LocalDateTime rangeStart, LocalDateTime rangeEnd, PageRequest page);
}
