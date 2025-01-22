package ru.practicum.ewm.main.event.service;

import ru.practicum.ewm.main.event.dto.EventFullDto;
import ru.practicum.ewm.main.event.dto.NewEventDto;
import ru.practicum.ewm.main.event.dto.UpdateEventUserRequest;

import java.util.List;

public interface EventService {
    EventFullDto addEvent(long userId, NewEventDto newEventDto);

    EventFullDto updateEvent(long userId, Long eventId, UpdateEventUserRequest updateEventUserRequest);

    EventFullDto findOwnersEventById(long userId, long eventId);

    List<EventFullDto> findOwnersEvents(long userId, int from, int size);
}
