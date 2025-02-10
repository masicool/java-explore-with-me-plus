package ru.practicum.ewm.main.request.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.ewm.main.request.model.Request;
import ru.practicum.ewm.main.request.model.Status;

import java.util.List;
import java.util.Optional;

public interface RequestRepository extends JpaRepository<Request, Long> {
    List<Request> findByRequesterId(long userId);

    List<Request> findByEventId(long eventId);

    List<Request> findByEventIdInAndStatus(List<Long> eventIds, Status status);

    Optional<Request> findByRequesterIdAndEventId(long userId, long eventId);

    long countByEventIdAndStatus(long eventId, Status status);
}
