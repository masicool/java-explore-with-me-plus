package ru.practicum.ewm.main.request.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.main.event.model.Event;
import ru.practicum.ewm.main.event.model.State;
import ru.practicum.ewm.main.event.repository.EventRepository;
import ru.practicum.ewm.main.exception.type.BadRequestException;
import ru.practicum.ewm.main.exception.type.ForbiddenException;
import ru.practicum.ewm.main.exception.type.NotFoundException;
import ru.practicum.ewm.main.request.dto.EventRequestStatusUpdateRequest;
import ru.practicum.ewm.main.request.dto.EventRequestStatusUpdateResult;
import ru.practicum.ewm.main.request.dto.ParticipationRequestDto;
import ru.practicum.ewm.main.request.mapper.RequestMapper;
import ru.practicum.ewm.main.request.model.Request;
import ru.practicum.ewm.main.request.model.Status;
import ru.practicum.ewm.main.request.model.UpdateRequestStatus;
import ru.practicum.ewm.main.request.repository.RequestRepository;
import ru.practicum.ewm.main.user.model.User;
import ru.practicum.ewm.main.user.repository.UserRepository;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class RequestServiceImpl implements RequestService {

    RequestRepository requestRepository;
    UserRepository userRepository;
    EventRepository eventRepository;

    @Override
    @Transactional(readOnly = true)
    public List<ParticipationRequestDto> findUserRequests(long userId) {
        receiveUser(userId);
        return requestRepository.findByRequesterId(userId).stream()
                .map(RequestMapper::mapToParticipationRequestDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ParticipationRequestDto> findAllRequestsOfEvent(long userId, long eventId) {
        User user = receiveUser(userId);
        Event event = receiveEvent(eventId);
        checkInitiatorOfEvent(user, event);
        return requestRepository.findByEventId(eventId).stream()
                .map(RequestMapper::mapToParticipationRequestDto)
                .toList();
    }

    @Override
    public ParticipationRequestDto createRequest(long userId, long eventId) {
        User user = receiveUser(userId);
        Event event = receiveEvent(eventId);
        if (event.getInitiator().equals(user)) {
            throw new ForbiddenException("Request for the event " + eventId + " is not for the initiator " + userId);
        }
        if (requestRepository.findByRequesterIdAndEventId(userId, eventId).isPresent()) {
            throw new ForbiddenException("User " + userId + " already has request for event " + event);
        }
        if (event.getState() != State.PUBLISHED) {
            throw new ForbiddenException("Event " + eventId + " is not in PUBLISHED");
        }
        if (event.getParticipantLimit() != 0) {
            long amountOfConfirmedRequests = requestRepository.countByEventIdAndStatus(eventId, Status.CONFIRMED);
            if (event.getParticipantLimit() <= amountOfConfirmedRequests) {
                throw new ForbiddenException("The limit of participants has been reached");
            }
        }
        Request request = RequestMapper.mapToRequest(user, event);
        if (!event.isRequestModeration() || event.getParticipantLimit() == 0) {
            request.setStatus(Status.CONFIRMED);
        }
        return RequestMapper.mapToParticipationRequestDto(requestRepository.save(request));
    }

    @Override
    public EventRequestStatusUpdateResult updateStatusForRequestsOfEvent(long userId, long eventId, EventRequestStatusUpdateRequest eventRequestStatusUpdateRequest) {
        User user = receiveUser(userId);
        Event event = receiveEvent(eventId);
        checkInitiatorOfEvent(user, event);
        if ((!event.isRequestModeration() || event.getParticipantLimit() == 0) && eventRequestStatusUpdateRequest.getStatus() == UpdateRequestStatus.CONFIRMED) {
            throw new BadRequestException("Confirmation of requests is not required for event " + eventId);
        }
        List<Request> requests = eventRequestStatusUpdateRequest.getRequestIds().stream()
                .map(this::receiveRequest)
                .peek(request -> {
                    if (!request.getEvent().equals(event)) {
                        throw new BadRequestException("Request " + request.getId() + " has not created for event " + eventId);
                    }
                })
                .peek(request -> {
                    if (request.getStatus() != Status.PENDING) {
                        throw new ForbiddenException("Request " + request.getId() + " is not in PENDING");
                    }
                })
                .toList();
        long amountOfConfirmedRequests = requestRepository.countByEventIdAndStatus(eventId, Status.CONFIRMED);
        if (event.getParticipantLimit() <= amountOfConfirmedRequests) {
            throw new ForbiddenException("The limit of participants has been reached");
        }
        if (eventRequestStatusUpdateRequest.getStatus() == UpdateRequestStatus.CONFIRMED) {
            List<Request> confirmedRequests = new ArrayList<>();
            List<Request> rejectedRequests = new ArrayList<>();
            for (Request request : requests) {
                if (event.getParticipantLimit() > amountOfConfirmedRequests) {
                    request.setStatus(Status.CONFIRMED);
                    confirmedRequests.add(request);
                    amountOfConfirmedRequests++;
                } else {
                    request.setStatus(Status.REJECTED);
                    rejectedRequests.add(request);
                }
            }
            requestRepository.saveAll(requests);
            return RequestMapper.mapToEventRequestStatusUpdateResult(confirmedRequests, rejectedRequests);
        } else {
            for (Request request : requests) {
                request.setStatus(Status.REJECTED);
            }
            requestRepository.saveAll(requests);
            return RequestMapper.mapToEventRequestStatusUpdateResult(List.of(), requests);
        }
    }

    @Override
    public ParticipationRequestDto cancelUserRequest(long userId, long requestId) {
        User user = receiveUser(userId);
        Request request = receiveRequest(requestId);
        if (!request.getRequester().equals(user)) {
            throw new BadRequestException("Request " + request.getId() + " has not made by user " + user.getId());
        }
        request.setStatus(Status.CANCELED);
        return RequestMapper.mapToParticipationRequestDto(requestRepository.save(request));
    }

    private User receiveUser(long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User with id=" + userId + " was not found"));
    }

    private Event receiveEvent(long eventId) {
        return eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Event with id=" + eventId + " was not found"));
    }

    private Request receiveRequest(long requestId) {
        return requestRepository.findById(requestId)
                .orElseThrow(() -> new NotFoundException("Request with id=" + requestId + " was not found"));
    }

    private void checkInitiatorOfEvent(User user, Event event) {
        if (!event.getInitiator().equals(user))
            throw new BadRequestException("User " + user.getId() + " is not initiator of event " + event.getId());
    }
}
