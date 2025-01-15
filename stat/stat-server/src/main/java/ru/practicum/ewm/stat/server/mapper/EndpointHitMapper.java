package ru.practicum.ewm.stat.server.mapper;

import ru.practicum.ewm.stat.dto.EndpointHitDto;
import ru.practicum.ewm.stat.server.model.EndpointHit;

public class EndpointHitMapper {
    public EndpointHit toEndpointHit(EndpointHitDto dto) {
        return EndpointHit.builder()
                .app(dto.getApp())
                .uri(dto.getUri())
                .ip(dto.getIp())
                .timestamp(dto.getTimestamp())
                .build();
    }

    public EndpointHitDto toEndPointHitDto(EndpointHit hit) {
        return EndpointHitDto.builder()
                .app(hit.getApp())
                .uri(hit.getUri())
                .ip(hit.getIp())
                .timestamp(hit.getTimestamp())
                .build();
    }
}
