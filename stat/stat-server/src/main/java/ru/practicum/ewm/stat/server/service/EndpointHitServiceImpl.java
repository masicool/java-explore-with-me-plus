package ru.practicum.ewm.stat.server.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.ewm.stat.server.model.EndpointHit;
import ru.practicum.ewm.stat.server.model.ViewStats;
import ru.practicum.ewm.stat.server.repository.EndpointHitRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class EndpointHitServiceImpl implements EndpointHitService {
    private final EndpointHitRepository statRepository;

    @Override
    public EndpointHit create(EndpointHit endpointHit) {
        return statRepository.save(endpointHit);
    }

    @Override
    public List<ViewStats> getStats(LocalDateTime start, LocalDateTime end, List<String> uris, boolean unique) {
        if (uris == null || uris.isEmpty()) {
            if (unique) {
                return statRepository.getAllUniqueStats(start, end);
            }
            return statRepository.getAllStats(start, end);
        }
        if (unique) {
            return statRepository.getUniqueStats(start, end, uris);
        }
        return statRepository.getStats(start, end, uris);
    }
}
