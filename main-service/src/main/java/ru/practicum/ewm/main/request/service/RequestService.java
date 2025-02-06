package ru.practicum.ewm.main.request.service;

import ru.practicum.ewm.main.request.dto.EventRequestStatusUpdateRequest;
import ru.practicum.ewm.main.request.dto.EventRequestStatusUpdateResult;
import ru.practicum.ewm.main.request.dto.ParticipationRequestDto;

import java.util.List;

public interface RequestService {
    List<ParticipationRequestDto> findUserRequests(long userId);

    List<ParticipationRequestDto> findAllRequestsOfEvent(long userId, long eventId);

    ParticipationRequestDto createRequest(long userId, long eventId);

    EventRequestStatusUpdateResult updateStatusForRequestsOfEvent(long userId, long eventId, EventRequestStatusUpdateRequest eventRequestStatusUpdateRequest);

    ParticipationRequestDto cancelUserRequest(long userId, long requestId);
}
