package ru.practicum.ewm.stat.server.controller;

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

    @PostMapping("/hit")
    @ResponseStatus(HttpStatus.CREATED)
    public EndpointHitDto addHit(@RequestBody EndpointHitDto endpointHitDto) {
        EndpointHit endpointHit = endpointHitService.create(modelMapper.map(endpointHitDto, EndpointHit.class));
        return modelMapper.map(endpointHit, EndpointHitDto.class);
    }

    @GetMapping("/stats")
    public List<ViewStatsDto> getStats(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime start,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")  LocalDateTime end,
            @RequestParam(required = false) List<String> uris,
            @RequestParam(required = false, defaultValue = "false") boolean unique) {

        return endpointHitService.getStats(start, end, uris, unique).stream().map(
                endpointHit -> modelMapper.map(endpointHit, ViewStatsDto.class)).toList();
    }
}
