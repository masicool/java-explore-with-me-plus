package ru.practicum.ewm.main.request.mapper;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import ru.practicum.ewm.main.event.model.Event;
import ru.practicum.ewm.main.request.dto.EventRequestStatusUpdateResult;
import ru.practicum.ewm.main.request.dto.ParticipationRequestDto;
import ru.practicum.ewm.main.request.model.Request;
import ru.practicum.ewm.main.request.model.Status;
import ru.practicum.ewm.main.user.model.User;

import java.time.LocalDateTime;
import java.util.List;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class RequestMapper {
    public static Request mapToRequest(User requester, Event event) {
        Request request = new Request();
        request.setCreated(LocalDateTime.now());
        request.setEvent(event);
        request.setRequester(requester);
        request.setStatus(Status.PENDING);
        return request;
    }

    public static ParticipationRequestDto mapToParticipationRequestDto(Request request) {
        return new ParticipationRequestDto(request.getCreated(), request.getEvent().getId(), request.getId(), request.getRequester().getId(), request.getStatus());
    }

    public static EventRequestStatusUpdateResult mapToEventRequestStatusUpdateResult(List<Request> confirmedRequests, List<Request> rejectedRequests) {
        return new EventRequestStatusUpdateResult(
                confirmedRequests.stream()
                        .map(RequestMapper::mapToParticipationRequestDto)
                        .toList(),
                rejectedRequests.stream()
                        .map(RequestMapper::mapToParticipationRequestDto)
                        .toList()
        );
    }
}
