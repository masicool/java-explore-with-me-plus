package ru.practicum.ewm.main.event.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.main.event.dto.EventFullDto;
import ru.practicum.ewm.main.event.dto.EventShortDto;
import ru.practicum.ewm.main.event.dto.FindAllEventsPublicParamEntity;
import ru.practicum.ewm.main.event.model.EventSortAction;
import ru.practicum.ewm.main.event.service.EventService;
import ru.practicum.ewm.stat.client.StatClient;
import ru.practicum.ewm.stat.dto.EndpointHitDto;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/events")
@RequiredArgsConstructor
public class PublicEventController {

    private static final String DATE_TIME_PATTERN = "yyyy-MM-dd HH:mm:ss";
    private final EventService eventService;
    private final StatClient statClient;

    @Value("${app.name}")
    private String appName;

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<EventShortDto> findAllEventsPublic(
            @RequestParam(required = false) @Size(min = 1, max = 7000) String text,
            @RequestParam(required = false) List<Long> categories,
            @RequestParam(required = false) Boolean paid,
            @RequestParam(required = false) @DateTimeFormat(pattern = DATE_TIME_PATTERN) LocalDateTime rangeStart,
            @RequestParam(required = false) @DateTimeFormat(pattern = DATE_TIME_PATTERN) LocalDateTime rangeEnd,
            @RequestParam(defaultValue = "false") boolean onlyAvailable,
            @RequestParam(required = false) EventSortAction sort,
            @RequestParam(defaultValue = "0") @PositiveOrZero int from,
            @RequestParam(defaultValue = "10") @Positive int size,
            HttpServletRequest request) {
        FindAllEventsPublicParamEntity findAllEventsPublicParamEntity = new FindAllEventsPublicParamEntity(text, categories, paid, rangeStart, rangeEnd, onlyAvailable, sort, from, size);
        List<EventShortDto> events = eventService.findAllEventsPublic(findAllEventsPublicParamEntity);
        EndpointHitDto endpointHitDto = new EndpointHitDto(appName, request.getRequestURI(), request.getRemoteAddr(), LocalDateTime.now());
        statClient.hit(endpointHitDto);
        return events;
    }

    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public EventFullDto findEvent(@PathVariable long id, HttpServletRequest request) {
        EndpointHitDto endpointHitDto = new EndpointHitDto("main-service", request.getRequestURI(), request.getRemoteAddr(), LocalDateTime.now());
        statClient.hit(endpointHitDto);
        return eventService.findEvent(id);
    }
}
