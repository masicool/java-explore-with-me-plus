package ru.practicum.ewm.stat.server.controller;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.stat.dto.EndpointHitDto;
import ru.practicum.ewm.stat.dto.ViewStatsDto;
import ru.practicum.ewm.stat.server.model.EndpointHit;
import ru.practicum.ewm.stat.server.service.EndpointHitService;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequiredArgsConstructor
public class StatController {
    private final EndpointHitService endpointHitService;
    private final ModelMapper modelMapper;
    private static final String DATE_TIME_PATTERN = "yyyy-MM-dd HH:mm:ss";

    @PostMapping("/hit")
    @ResponseStatus(HttpStatus.CREATED)
    public EndpointHitDto addHit(@Valid @RequestBody EndpointHitDto endpointHitDto) {
        EndpointHit endpointHit = endpointHitService.create(modelMapper.map(endpointHitDto, EndpointHit.class));
        return modelMapper.map(endpointHit, EndpointHitDto.class);
    }

    @GetMapping("/stats")
    public List<ViewStatsDto> getStats(
            @RequestParam @DateTimeFormat(pattern = DATE_TIME_PATTERN) LocalDateTime start,
            @RequestParam @DateTimeFormat(pattern = DATE_TIME_PATTERN)  LocalDateTime end,
            @RequestParam(required = false) List<String> uris,
            @RequestParam(required = false, defaultValue = "false") boolean unique) {

        return endpointHitService.getStats(start, end, uris, unique).stream().map(
                endpointHit -> modelMapper.map(endpointHit, ViewStatsDto.class)).toList();
    }
}
