package ru.practicum.ewm.main.event.service;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Predicate;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.main.category.model.Category;
import ru.practicum.ewm.main.category.repository.CategoryRepository;
import ru.practicum.ewm.main.comment.model.Comment;
import ru.practicum.ewm.main.comment.repository.CommentRepository;
import ru.practicum.ewm.main.event.dto.*;
import ru.practicum.ewm.main.event.mapper.EventMapper;
import ru.practicum.ewm.main.event.mapper.LocationMapper;
import ru.practicum.ewm.main.event.model.*;
import ru.practicum.ewm.main.event.repository.EventRepository;
import ru.practicum.ewm.main.event.repository.LocationRepository;
import ru.practicum.ewm.main.exception.type.BadRequestException;
import ru.practicum.ewm.main.exception.type.ForbiddenException;
import ru.practicum.ewm.main.exception.type.NotFoundException;
import ru.practicum.ewm.main.request.model.Request;
import ru.practicum.ewm.main.request.model.Status;
import ru.practicum.ewm.main.request.repository.RequestRepository;
import ru.practicum.ewm.main.user.model.User;
import ru.practicum.ewm.main.user.repository.UserRepository;
import ru.practicum.ewm.stat.client.StatClient;
import ru.practicum.ewm.stat.dto.ViewStatsDto;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Transactional
public class EventServiceImpl implements EventService {
    EventRepository eventRepository;
    LocationRepository locationRepository;
    UserRepository userRepository;
    CategoryRepository categoryRepository;
    StatClient statClient;
    RequestRepository requestRepository;
    CommentRepository commentRepository;

    @Override
    public EventFullDto addEvent(long userId, NewEventDto newEventDto) {
        if (newEventDto.getEventDate().isBefore(LocalDateTime.now().plusHours(2))) {
            throw new BadRequestException("EventDate must be after now");
        }
        User user = receiveUser(userId);
        Category category = receiveCategory(newEventDto.getCategory());
        Location location = addLocation(newEventDto.getLocation()); // TODO Под фичу нужно будет пересмотреть эту логику, а пока что так.
        Event event = EventMapper.mapToEvent(user, category, location, newEventDto);
        return EventMapper.mapToEventFullDto(eventRepository.save(event));
    }

    @Override
    public EventFullDto updateEvent(long userId, long eventId, UpdateEventUserRequestDto updateEventUserRequestDto) {
        User user = receiveUser(userId);
        Event event = receiveEvent(eventId);
        checkValidUserForEvent(user, event);
        if (event.getState() == State.PUBLISHED) {
            throw new ForbiddenException("Event must not be published");
        }
        UpdateEventFieldsEntity updateEventFieldsEntity = new UpdateEventFieldsEntity(updateEventUserRequestDto.getAnnotation(),
                updateEventUserRequestDto.getCategory(),
                updateEventUserRequestDto.getDescription(),
                updateEventUserRequestDto.getEventDate(),
                updateEventUserRequestDto.getLocation(),
                updateEventUserRequestDto.getPaid(),
                updateEventUserRequestDto.getParticipantLimit(),
                updateEventUserRequestDto.getRequestModeration(),
                updateEventUserRequestDto.getTitle()
        );
        updateFields(event, updateEventFieldsEntity);
        if (updateEventUserRequestDto.getStateAction() != null) {
            switch (updateEventUserRequestDto.getStateAction()) {
                case SEND_TO_REVIEW -> event.setState(State.PENDING);
                case CANCEL_REVIEW -> event.setState(State.CANCELED);
            }
        }
        return EventMapper.mapToEventFullDto(eventRepository.save(event));
    }

    @Override
    @Transactional(readOnly = true)
    public EventFullDto findOwnersEventById(long userId, long eventId) {
        User user = receiveUser(userId);
        Event event = receiveEvent(eventId);
        checkValidUserForEvent(user, event);
        return loadStatisticAndRequest(EventMapper.mapToEventFullDto(event));
    }

    @Override
    @Transactional(readOnly = true)
    public List<EventFullDto> findOwnersEvents(long userId, int from, int size) {
        User user = receiveUser(userId);
        PageRequest page = PageRequest.of(from, size);
        return loadStatisticAndRequestForList(eventRepository.findAllByInitiatorId(user.getId(), page).stream()
                .map(EventMapper::mapToEventFullDto)
                .toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<EventFullDto> findAllEvents(FindAllEventsParamEntity findAllEventsParamEntity) {
        Predicate predicate = predicateForFindingAllEventsByAdmin(findAllEventsParamEntity);
        PageRequest page = PageRequest.of(findAllEventsParamEntity.getFrom(), findAllEventsParamEntity.getSize());
        return loadStatisticAndRequestForList(eventRepository.findAll(predicate, page).stream()
                .map(EventMapper::mapToEventFullDto)
                .toList());
    }

    @Override
    @Transactional
    public EventFullDto editEvent(long eventId, UpdateEventAdminRequestDto updateEventAdminRequestDto) {
        Event event = receiveEvent(eventId);
        if (updateEventAdminRequestDto.getEventDate() != null && updateEventAdminRequestDto.getEventDate().isBefore(event.getCreated().plusHours(1))) {
            throw new BadRequestException("Date of event cannot be before created date!");
        }
        UpdateEventFieldsEntity updateEventFieldsEntity = new UpdateEventFieldsEntity(updateEventAdminRequestDto.getAnnotation(),
                updateEventAdminRequestDto.getCategory(),
                updateEventAdminRequestDto.getDescription(),
                updateEventAdminRequestDto.getEventDate(),
                updateEventAdminRequestDto.getLocation(),
                updateEventAdminRequestDto.getPaid(),
                updateEventAdminRequestDto.getParticipantLimit(),
                updateEventAdminRequestDto.getRequestModeration(),
                updateEventAdminRequestDto.getTitle()
        );
        updateFields(event, updateEventFieldsEntity);
        if (updateEventAdminRequestDto.getStateAction() != null) {
            if (event.getState() != State.PENDING) {
                throw new ForbiddenException("Cannot publish the event because it is not in PENDING");
            }
            switch (updateEventAdminRequestDto.getStateAction()) {
                case PUBLISH_EVENT -> {
                    event.setState(State.PUBLISHED);
                    event.setPublished(LocalDateTime.now());
                }
                case REJECT_EVENT -> event.setState(State.CANCELED);
            }
        }
        return loadStatisticAndRequest(EventMapper.mapToEventFullDto(eventRepository.save(event)));
    }

    @Override
    @Transactional(readOnly = true)
    public List<EventShortDto> findAllEventsPublic(FindAllEventsPublicParamEntity findAllEventsPublicParamEntity) {
        LocalDateTime rangeEnd = findAllEventsPublicParamEntity.getRangeEnd();
        LocalDateTime rangeStart = findAllEventsPublicParamEntity.getRangeStart();
        if (rangeEnd != null && rangeStart != null) {
            if (rangeEnd.isBefore(rangeStart)) {
                throw new BadRequestException("'rangeEnd' can not be before 'rangeStart'");
            }
        }
        Predicate predicate = predicateForFindingAllEventsByAny(findAllEventsPublicParamEntity);
        PageRequest page = PageRequest.of(findAllEventsPublicParamEntity.getFrom(), findAllEventsPublicParamEntity.getSize());
        List<Event> events = eventRepository.findAll(predicate, page).stream().toList();
        if (findAllEventsPublicParamEntity.isOnlyAvailable()) {
            events = events.stream()
                    .filter(this::isEventAvailableByLimit)
                    .toList();
        }
        List<EventShortDto> eventShortDtos = loadStatisticAndRequest(events);
        if (findAllEventsPublicParamEntity.getSort() != null) {
            switch (findAllEventsPublicParamEntity.getSort()) {
                case EVENT_DATE -> eventShortDtos = eventShortDtos.stream()
                        .sorted(Comparator.comparing(EventShortDto::getEventDate))
                        .toList();
                case VIEWS -> eventShortDtos = eventShortDtos.stream()
                        .sorted(Comparator.comparing(EventShortDto::getViews))
                        .toList();
            }
        }
        return eventShortDtos;
    }

    @Override
    @Transactional(readOnly = true)
    public EventFullDto findEvent(long eventId) {
        Event event = receiveEvent(eventId);
        if (event.getState() != State.PUBLISHED) {
            throw new NotFoundException("Event with id=" + eventId + " was not found");
        }
        return loadStatisticAndRequest(EventMapper.mapToEventFullDto(event));
    }

    private void updateFields(Event event, UpdateEventFieldsEntity updateEventFieldsEntity) {
        if (updateEventFieldsEntity.hasEventDate()) {
            if (updateEventFieldsEntity.getEventDate().isBefore(LocalDateTime.now().plusHours(2))) {
                throw new BadRequestException("The date and time of event cannot be earlier than two hours from the current moment.");
            }
            event.setEventDate(updateEventFieldsEntity.getEventDate());
        }
        if (updateEventFieldsEntity.hasAnnotation()) {
            event.setAnnotation(updateEventFieldsEntity.getAnnotation());
        }
        if (updateEventFieldsEntity.hasCategoryId()) {
            Category category = receiveCategory(updateEventFieldsEntity.getCategoryId());
            event.setCategory(category);
        }
        if (updateEventFieldsEntity.hasDescription()) {
            event.setDescription(updateEventFieldsEntity.getDescription());
        }
        if (updateEventFieldsEntity.hasLocation()) { //TODO Эту реализацию также изменить под фичу.
            //  (Тут id локации такой же, меняются значения)
            event.setLocation(locationRepository.save(LocationMapper.updateLocationFields(event.getLocation(), updateEventFieldsEntity.getLocation())));
        }
        if (updateEventFieldsEntity.hasPaid()) {
            event.setPaid(updateEventFieldsEntity.getPaid());
        }
        if (updateEventFieldsEntity.hasParticipantLimit()) {
            event.setParticipantLimit(updateEventFieldsEntity.getParticipantLimit());
        }
        if (updateEventFieldsEntity.hasRequestModeration()) {
            event.setRequestModeration(updateEventFieldsEntity.getRequestModeration());
        }
        if (updateEventFieldsEntity.hasTitle()) {
            event.setTitle(updateEventFieldsEntity.getTitle());
        }
    }

    private List<EventShortDto> loadStatisticAndRequest(List<Event> events) {
        if (events == null || events.isEmpty()) {
            return List.of();
        }
        LocalDateTime start = events.stream()
                .map(Event::getCreated)
                .min(LocalDateTime::compareTo)
                .get();
        List<String> uris = events.stream()
                .map(event -> "/events/" + event.getId())
                .toList();
        List<Request> requests = requestRepository.findByEventIdInAndStatus(events.stream()
                .map(Event::getId)
                .toList(), Status.CONFIRMED);
        List<ViewStatsDto> viewStats = statClient.getStat(start, LocalDateTime.now(), uris, true);
        List<Comment> comments = commentRepository.findByEventIdIn(events.stream()
                .map(Event::getId)
                .toList());
        return events.stream()
                .map(EventMapper::mapToEventShortDto)
                .peek(event -> event.setConfirmedRequests(requests.stream()
                        .filter(request -> request.getEvent().getId() == event.getId())
                        .count()))
                .peek(event -> event.setViews(viewStats.stream()
                        .filter(view -> view.getUri().equals("/events/" + event.getId()))
                        .map(ViewStatsDto::getHits)
                        .reduce(0L, Long::sum)))
                .peek(event -> event.setComments(comments.stream()
                        .filter(comment -> comment.getEvent().getId() == event.getId())
                        .count()))
                .toList();
    }

    private EventFullDto loadStatisticAndRequest(EventFullDto event) {
        long amountOfConfirmedRequests = requestRepository.countByEventIdAndStatus(event.getId(), Status.CONFIRMED);
        event.setConfirmedRequests(amountOfConfirmedRequests);
        long amountOfViews = statClient.getStat(event.getCreatedOn(), LocalDateTime.now(), List.of("/events/" + event.getId()), true).stream()
                .map(ViewStatsDto::getHits)
                .reduce(0L, Long::sum);
        event.setViews(amountOfViews);
        long amountOfComments = commentRepository.countByEventId(event.getId());
        event.setComments(amountOfComments);
        return event;
    }

    private List<EventFullDto> loadStatisticAndRequestForList(List<EventFullDto> events) {
        if (events == null || events.isEmpty()) {
            return List.of();
        }
        LocalDateTime start = events.stream()
                .map(EventFullDto::getCreatedOn)
                .min(LocalDateTime::compareTo)
                .get();
        List<String> uris = events.stream()
                .map(event -> "/events/" + event.getId())
                .toList();
        List<Request> requests = requestRepository.findByEventIdInAndStatus(events.stream()
                .map(EventFullDto::getId)
                .toList(), Status.CONFIRMED);
        List<ViewStatsDto> viewStats = statClient.getStat(start, LocalDateTime.now(), uris, true);
        List<Comment> comments = commentRepository.findByEventIdIn(events.stream()
                .map(EventFullDto::getId)
                .toList());
        return events.stream()
                .peek(event -> event.setConfirmedRequests(requests.stream()
                        .filter(request -> request.getEvent().getId() == event.getId())
                        .count()))
                .peek(event -> event.setViews(viewStats.stream()
                        .filter(view -> view.getUri().equals("/events/" + event.getId()))
                        .map(ViewStatsDto::getHits)
                        .reduce(0L, Long::sum)))
                .peek(event -> event.setComments(comments.stream()
                        .filter(comment -> comment.getEvent().getId() == event.getId())
                        .count()))
                .toList();
    }

    private Predicate predicateForFindingAllEventsByAdmin(FindAllEventsParamEntity findAllEventsParamEntity) {
        BooleanBuilder booleanBuilder = new BooleanBuilder();
        if (findAllEventsParamEntity.getUsers() != null && !findAllEventsParamEntity.getUsers().isEmpty()) {
            booleanBuilder.and(QEvent.event.initiator.id.in(findAllEventsParamEntity.getUsers()));
        }
        if (findAllEventsParamEntity.getStates() != null && !findAllEventsParamEntity.getStates().isEmpty()) {
            booleanBuilder.and(QEvent.event.state.in(findAllEventsParamEntity.getStates()));
        }
        if (findAllEventsParamEntity.getCategories() != null && !findAllEventsParamEntity.getCategories().isEmpty()) {
            booleanBuilder.and(QEvent.event.category.id.in(findAllEventsParamEntity.getCategories()));
        }
        if (findAllEventsParamEntity.getRangeStart() != null) {
            booleanBuilder.and(QEvent.event.eventDate.after(findAllEventsParamEntity.getRangeStart()));
        }
        if (findAllEventsParamEntity.getRangeEnd() != null) {
            booleanBuilder.and(QEvent.event.eventDate.before(findAllEventsParamEntity.getRangeEnd()));
        }
        if (!booleanBuilder.hasValue()) booleanBuilder.and(QEvent.event.id.isNotNull());
        return booleanBuilder.getValue();
    }

    private Predicate predicateForFindingAllEventsByAny(FindAllEventsPublicParamEntity findAllEventsPublicParamEntity) {
        BooleanBuilder booleanBuilder = new BooleanBuilder();
        booleanBuilder.and(QEvent.event.state.eq(State.PUBLISHED));
        if (findAllEventsPublicParamEntity.getText() != null && !findAllEventsPublicParamEntity.getText().isBlank()) {
            booleanBuilder.and(QEvent.event.description.containsIgnoreCase(findAllEventsPublicParamEntity.getText())
                    .or(QEvent.event.annotation.containsIgnoreCase(findAllEventsPublicParamEntity.getText())));
        }
        if (findAllEventsPublicParamEntity.getCategories() != null && !findAllEventsPublicParamEntity.getCategories().isEmpty()) {
            booleanBuilder.and(QEvent.event.category.id.in(findAllEventsPublicParamEntity.getCategories()));
        }
        if (findAllEventsPublicParamEntity.getPaid() != null) {
            booleanBuilder.and(QEvent.event.paid.eq(findAllEventsPublicParamEntity.getPaid()));
        }
        if (findAllEventsPublicParamEntity.getRangeStart() != null) {
            booleanBuilder.and(QEvent.event.eventDate.after(findAllEventsPublicParamEntity.getRangeStart()));
        }
        if (findAllEventsPublicParamEntity.getRangeEnd() != null) {
            booleanBuilder.and(QEvent.event.eventDate.before(findAllEventsPublicParamEntity.getRangeEnd()));
        }
        if (findAllEventsPublicParamEntity.getRangeStart() != null && findAllEventsPublicParamEntity.getRangeEnd() != null) {
            booleanBuilder.and(QEvent.event.eventDate.after(LocalDateTime.now()));
        }
        return booleanBuilder.getValue();
    }

    private User receiveUser(long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User with id=" + userId + " was not found"));
    }

    private Category receiveCategory(long catId) {
        return categoryRepository.findById(catId)
                .orElseThrow(() -> new NotFoundException("Category with id=" + catId + " was not found"));
    }

    private Event receiveEvent(long eventId) {
        return eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Event with id=" + eventId + " was not found"));
    }

    private void checkValidUserForEvent(User user, Event event) {
        if (!event.getInitiator().equals(user))
            throw new BadRequestException("Event is not for this user");
    }

    private boolean isEventAvailableByLimit(Event event) {
        return event.getParticipantLimit() > requestRepository.countByEventIdAndStatus(event.getId(), Status.CONFIRMED);
    }

    private Location addLocation(LocationDto locationDto) {
        return locationRepository.save(LocationMapper.mapToLocation(locationDto));
    }
}
