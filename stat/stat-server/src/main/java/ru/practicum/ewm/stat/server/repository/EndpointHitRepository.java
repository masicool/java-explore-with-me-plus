package ru.practicum.ewm.stat.server.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.ewm.stat.server.model.EndpointHit;
import ru.practicum.ewm.stat.server.model.ViewStats;

import java.time.LocalDateTime;
import java.util.List;

public interface EndpointHitRepository extends JpaRepository<EndpointHit, Integer> {

    /**
     * Количество просмотров для переданных uri в (разрезе приложений)
     */
    @Query("select new ru.practicum.ewm.stat.server.model.ViewStats(eh.app, eh.uri, COUNT(eh.id)) " +
            "from EndpointHit eh " +
            "where eh.timestamp between :start and :end " +
            "and (:uris is null or eh.uri in :uris) " +
            "group by eh.app, eh.uri " +
            "ORDER BY COUNT(eh.id) DESC")
    List<ViewStats> getStats(LocalDateTime start, LocalDateTime end, List<String> uris);

    /**
     * Количество уникальных ip, с которых просматривали переданные uri (в разрезе приложений)
     */
    @Query("select new ru.practicum.ewm.stat.server.model.ViewStats(eh.app, eh.uri, COUNT(DISTINCT eh.ip)) " +
            "from EndpointHit eh " +
            "where eh.timestamp between :start and :end " +
            "and (:uris is null or eh.uri in :uris) " +
            "group by eh.app, eh.uri " +
            "ORDER BY COUNT(DISTINCT eh.ip) DESC")
    List<ViewStats> getUniqueStats(LocalDateTime start, LocalDateTime end, List<String> uris);
}
