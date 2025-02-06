package ru.practicum.ewm.main.event.service;

import ru.practicum.ewm.main.event.dto.*;

import java.util.List;

public interface EventService {
    EventFullDto addEvent(long userId, NewEventDto newEventDto);

    EventFullDto updateEvent(long userId, long eventId, UpdateEventUserRequestDto updateEventUserRequestDto);

    EventFullDto findOwnersEventById(long userId, long eventId);

    List<EventFullDto> findOwnersEvents(long userId, int from, int size);

    List<EventFullDto> findAllEvents(FindAllEventsParamEntity findAllEventsParamEntity);

    EventFullDto editEvent(long eventId, UpdateEventAdminRequestDto updateEventAdminRequestDto);

    List<EventShortDto> findAllEventsPublic(FindAllEventsPublicParamEntity findAllEventsPublicParamEntity);

    EventFullDto findEvent(long eventId);
}
