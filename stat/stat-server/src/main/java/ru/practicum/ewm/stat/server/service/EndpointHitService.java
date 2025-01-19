package ru.practicum.ewm.stat.server.service;

import ru.practicum.ewm.stat.server.model.EndpointHit;
import ru.practicum.ewm.stat.server.model.ViewStats;

import java.time.LocalDateTime;
import java.util.List;

public interface EndpointHitService {
    EndpointHit create(EndpointHit endpointHit);

    List<ViewStats> getStats(LocalDateTime start, LocalDateTime end, List<String> uris, boolean unique);
}
