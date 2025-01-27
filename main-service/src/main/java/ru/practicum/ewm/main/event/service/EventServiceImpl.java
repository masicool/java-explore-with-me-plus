package ru.practicum.ewm.main.event.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.main.category.model.Category;
import ru.practicum.ewm.main.category.repository.CategoryRepository;
import ru.practicum.ewm.main.event.dto.*;
import ru.practicum.ewm.main.event.mapper.EventMapper;
import ru.practicum.ewm.main.event.mapper.LocationMapper;
import ru.practicum.ewm.main.event.model.*;
import ru.practicum.ewm.main.event.repository.EventRepository;
import ru.practicum.ewm.main.event.repository.LocationRepository;
import ru.practicum.ewm.main.exception.type.BadRequestException;
import ru.practicum.ewm.main.exception.type.ForbiddenException;
import ru.practicum.ewm.main.exception.type.NotFoundException;
import ru.practicum.ewm.main.user.model.User;
import ru.practicum.ewm.main.user.repository.UserRepository;
import ru.practicum.ewm.stat.client.StatClient;
import ru.practicum.ewm.stat.dto.ViewStatsDto;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Transactional
public class EventServiceImpl implements EventService {
    final EventRepository eventRepository;
    final LocationRepository locationRepository;
    final UserRepository userRepository;
    final CategoryRepository categoryRepository;
    final StatClient statClient;

    @Override
    public EventFullDto addEvent(long userId, NewEventDto newEventDto) {
        if (newEventDto.getEventDate().isBefore(LocalDateTime.now().plusHours(2))) {
            throw new ForbiddenException("Field: eventDate. Error: должно содержать дату, которая еще не наступила. Value: " + newEventDto.getEventDate());
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
        if (event.getState() != State.PUBLISHED) {
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
    public EventFullDto findOwnersEventById(long userId, long eventId) {
        User user = receiveUser(userId);
        Event event = receiveEvent(eventId);
        checkValidUserForEvent(user, event);
        return loadStatisticAndRequest(EventMapper.mapToEventFullDto(event));
    }

    @Override
    public List<EventFullDto> findOwnersEvents(long userId, int from, int size) {
        User user = receiveUser(userId);
        PageRequest page = PageRequest.of(from, size);
        List<EventFullDto> events = eventRepository.findAllByInitiatorId(user.getId(), page).stream()
                .map(EventMapper::mapToEventFullDto)
                .toList();
        return loadStatisticAndRequestForList(events);
    }

    //TODO Чуть позже перепишу этот огромный метод снизу. Он рабочий, но поддерживать его и разобраться в нем неудобно.

    @Override
    public List<EventFullDto> findAllEvents(FindAllEventsParamEntity findAllEventsParamEntity) {
        PageRequest page = PageRequest.of(findAllEventsParamEntity.getFrom(), findAllEventsParamEntity.getSize());
        if (findAllEventsParamEntity.getUsers() == null || findAllEventsParamEntity.getUsers().isEmpty()) {
            if (findAllEventsParamEntity.getStates() == null || findAllEventsParamEntity.getStates().isEmpty()) {
                if (findAllEventsParamEntity.getCategories() == null || findAllEventsParamEntity.getCategories().isEmpty()) {
                    if (findAllEventsParamEntity.getRangeStart() == null) {
                        if (findAllEventsParamEntity.getRangeEnd() == null) {
                            // Юзеры null, Статус null, Категории null, Старт null, Конец null
                            return eventRepository.findAll(page).stream()
                                    .map(EventMapper::mapToEventFullDto)
                                    .peek(this::loadStatisticAndRequest)
                                    .toList();
                        }
                        // Юзеры null, Статус null, Категории null, Старт null, Конец есть
                        return eventRepository.findByEventDateBefore(findAllEventsParamEntity.getRangeEnd(), page).stream()
                                .map(EventMapper::mapToEventFullDto)
                                .peek(this::loadStatisticAndRequest)
                                .toList();
                    }
                    if (findAllEventsParamEntity.getRangeEnd() == null) {
                        // Юзеры null, Статус null, Категории null, Старт есть, Конец null
                        return eventRepository.findByEventDateAfter(findAllEventsParamEntity.getRangeStart(), page).stream()
                                .map(EventMapper::mapToEventFullDto)
                                .peek(this::loadStatisticAndRequest)
                                .toList();
                    }
                    // Юзеры null, Статус null, Категории null, Старт есть, Конец есть
                    return eventRepository.findByEventDateAfterAndEventDateBefore(findAllEventsParamEntity.getRangeStart(), findAllEventsParamEntity.getRangeEnd(), page).stream()
                            .map(EventMapper::mapToEventFullDto)
                            .peek(this::loadStatisticAndRequest)
                            .toList();
                }
                if (findAllEventsParamEntity.getRangeStart() == null) {
                    if (findAllEventsParamEntity.getRangeEnd() == null) {
                        // Юзеры null, Статус null, Категории есть, Старт null, Конец null
                        return eventRepository.findByCategoryIdIn(findAllEventsParamEntity.getCategories(), page).stream()
                                .map(EventMapper::mapToEventFullDto)
                                .peek(this::loadStatisticAndRequest)
                                .toList();
                    }
                    // Юзеры null, Статус null, Категории есть, Старт null, Конец есть
                    return eventRepository.findByCategoryIdInAndEventDateBefore(findAllEventsParamEntity.getCategories(), findAllEventsParamEntity.getRangeEnd(), page).stream()
                            .map(EventMapper::mapToEventFullDto)
                            .peek(this::loadStatisticAndRequest)
                            .toList();
                }
                if (findAllEventsParamEntity.getRangeEnd() == null) {
                    // Юзеры null, Статус null, Категории есть, Старт есть, Конец null
                    return eventRepository.findByCategoryIdInAndEventDateAfter(findAllEventsParamEntity.getCategories(), findAllEventsParamEntity.getRangeStart(), page).stream()
                            .map(EventMapper::mapToEventFullDto)
                            .peek(this::loadStatisticAndRequest)
                            .toList();
                }
                // Юзеры null, Статус null, Категории есть, Старт есть, Конец есть
                return eventRepository.findByCategoryIdInAndEventDateAfterAndEventDateBefore(findAllEventsParamEntity.getCategories(), findAllEventsParamEntity.getRangeStart(), findAllEventsParamEntity.getRangeEnd(), page).stream()
                        .map(EventMapper::mapToEventFullDto)
                        .peek(this::loadStatisticAndRequest)
                        .toList();
            }
            if (findAllEventsParamEntity.getCategories() == null || findAllEventsParamEntity.getCategories().isEmpty()) {
                if (findAllEventsParamEntity.getRangeStart() == null) {
                    if (findAllEventsParamEntity.getRangeEnd() == null) {
                        // Юзеры null, Статус есть, Категории null, Старт null, Конец null
                        return eventRepository.findByStateIn(findAllEventsParamEntity.getStates(), page).stream()
                                .map(EventMapper::mapToEventFullDto)
                                .peek(this::loadStatisticAndRequest)
                                .toList();
                    }
                    // Юзеры null, Статус есть, Категории null, Старт null, Конец есть
                    return eventRepository.findByStateInAndEventDateBefore(findAllEventsParamEntity.getStates(), findAllEventsParamEntity.getRangeEnd(), page).stream()
                            .map(EventMapper::mapToEventFullDto)
                            .peek(this::loadStatisticAndRequest)
                            .toList();
                }
                if (findAllEventsParamEntity.getRangeEnd() == null) {
                    // Юзеры null, Статус есть, Категории null, Старт есть, Конец null
                    return eventRepository.findByStateInAndEventDateAfter(findAllEventsParamEntity.getStates(), findAllEventsParamEntity.getRangeStart(), page).stream()
                            .map(EventMapper::mapToEventFullDto)
                            .peek(this::loadStatisticAndRequest)
                            .toList();
                }
                // Юзеры null, Статус есть, Категории null, Старт есть, Конец есть
                return eventRepository.findByStateInAndEventDateAfterAndEventDateBefore(findAllEventsParamEntity.getStates(), findAllEventsParamEntity.getRangeStart(), findAllEventsParamEntity.getRangeEnd(), page).stream()
                        .map(EventMapper::mapToEventFullDto)
                        .peek(this::loadStatisticAndRequest)
                        .toList();
            }
            if (findAllEventsParamEntity.getRangeStart() == null) {
                if (findAllEventsParamEntity.getRangeEnd() == null) {
                    //Юзеры null, Статус есть, Категории есть, Старт null, Конец null
                    return eventRepository.findByStateInAndCategoryIdIn(findAllEventsParamEntity.getStates(), findAllEventsParamEntity.getCategories(), page).stream()
                            .map(EventMapper::mapToEventFullDto)
                            .peek(this::loadStatisticAndRequest)
                            .toList();
                }
                //Юзеры null, Статус есть, Категории есть, Старт null, Конец есть
                return eventRepository.findByStateInAndCategoryIdInAndEventDateBefore(findAllEventsParamEntity.getStates(), findAllEventsParamEntity.getCategories(), findAllEventsParamEntity.getRangeEnd(), page).stream()
                        .map(EventMapper::mapToEventFullDto)
                        .peek(this::loadStatisticAndRequest)
                        .toList();
            }
            if (findAllEventsParamEntity.getRangeEnd() == null) {
                //Юзеры null, Статус есть, Категории есть, Старт есть, Конец null
                return eventRepository.findByStateInAndCategoryIdInAndEventDateAfter(findAllEventsParamEntity.getStates(), findAllEventsParamEntity.getCategories(), findAllEventsParamEntity.getRangeStart(), page).stream()
                        .map(EventMapper::mapToEventFullDto)
                        .peek(this::loadStatisticAndRequest)
                        .toList();
            }
            //Юзеры null, Статус есть, Категории есть, Старт есть, Конец есть
            return eventRepository.findByStateInAndCategoryIdInAndEventDateAfterAndEventDateBefore(findAllEventsParamEntity.getStates(), findAllEventsParamEntity.getCategories(), findAllEventsParamEntity.getRangeStart(), findAllEventsParamEntity.getRangeEnd(), page).stream()
                    .map(EventMapper::mapToEventFullDto)
                    .peek(this::loadStatisticAndRequest)
                    .toList();
        }
        if (findAllEventsParamEntity.getStates() == null || findAllEventsParamEntity.getStates().isEmpty()) {
            if (findAllEventsParamEntity.getCategories() == null || findAllEventsParamEntity.getCategories().isEmpty()) {
                if (findAllEventsParamEntity.getRangeStart() == null) {
                    if (findAllEventsParamEntity.getRangeEnd() == null) {
                        // Юзеры есть, Статус null, Категории null, Старт null, конец null
                        return eventRepository.findByInitiatorIdIn(findAllEventsParamEntity.getUsers(), page).stream()
                                .map(EventMapper::mapToEventFullDto)
                                .peek(this::loadStatisticAndRequest)
                                .toList();
                    }
                    // Юзеры есть, Статус null, Категории null, Старт null, Конец есть
                    return eventRepository.findByInitiatorIdInAndEventDateBefore(findAllEventsParamEntity.getUsers(), findAllEventsParamEntity.getRangeEnd(), page).stream()
                            .map(EventMapper::mapToEventFullDto)
                            .peek(this::loadStatisticAndRequest)
                            .toList();
                }
                if (findAllEventsParamEntity.getRangeEnd() == null) {
                    // Юзеры есть, Статус null, Категории null, Старт есть, Конец null
                    return eventRepository.findByInitiatorIdInAndEventDateAfter(findAllEventsParamEntity.getUsers(), findAllEventsParamEntity.getRangeStart(), page).stream()
                            .map(EventMapper::mapToEventFullDto)
                            .peek(this::loadStatisticAndRequest)
                            .toList();
                }
                // Юзеры есть, Статус null, Категории null, Старт есть, Конец есть
                return eventRepository.findByInitiatorIdInAndEventDateAfterAndEventDateBefore(findAllEventsParamEntity.getUsers(), findAllEventsParamEntity.getRangeStart(), findAllEventsParamEntity.getRangeEnd(), page).stream()
                        .map(EventMapper::mapToEventFullDto)
                        .peek(this::loadStatisticAndRequest)
                        .toList();
            }
            if (findAllEventsParamEntity.getRangeStart() == null) {
                if (findAllEventsParamEntity.getRangeEnd() == null) {
                    // Юзеры есть, Статус null, Категории есть, Старт null, конец null
                    return eventRepository.findByInitiatorIdInAndCategoryIdIn(findAllEventsParamEntity.getUsers(), findAllEventsParamEntity.getCategories(), page).stream()
                            .map(EventMapper::mapToEventFullDto)
                            .peek(this::loadStatisticAndRequest)
                            .toList();
                }
                // Юзеры есть, Статус null, Категории есть, Старт null, Конец есть
                return eventRepository.findByInitiatorIdInAndCategoryIdInAndEventDateBefore(findAllEventsParamEntity.getUsers(), findAllEventsParamEntity.getCategories(), findAllEventsParamEntity.getRangeEnd(), page).stream()
                        .map(EventMapper::mapToEventFullDto)
                        .peek(this::loadStatisticAndRequest)
                        .toList();
            }
            if (findAllEventsParamEntity.getRangeEnd() == null) {
                // Юзеры есть, Статус null, Категории есть, Старт есть, Конец null
                return eventRepository.findByInitiatorIdInAndCategoryIdInAndEventDateAfter(findAllEventsParamEntity.getUsers(), findAllEventsParamEntity.getCategories(), findAllEventsParamEntity.getRangeStart(), page).stream()
                        .map(EventMapper::mapToEventFullDto)
                        .peek(this::loadStatisticAndRequest)
                        .toList();
            }
            // Юзеры есть, Статус null, Категории есть, Старт есть, Конец есть
            return eventRepository.findByInitiatorIdInAndCategoryIdInAndEventDateAfterAndEventDateBefore(findAllEventsParamEntity.getUsers(), findAllEventsParamEntity.getCategories(), findAllEventsParamEntity.getRangeStart(), findAllEventsParamEntity.getRangeEnd(), page).stream()
                    .map(EventMapper::mapToEventFullDto)
                    .peek(this::loadStatisticAndRequest)
                    .toList();
        }
        if (findAllEventsParamEntity.getCategories() == null || findAllEventsParamEntity.getCategories().isEmpty()) {
            if (findAllEventsParamEntity.getRangeStart() == null) {
                if (findAllEventsParamEntity.getRangeEnd() == null) {
                    // Юзеры есть, Статус есть, Категории null, Старт null, конец null
                    return eventRepository.findByInitiatorIdInAndStateIn(findAllEventsParamEntity.getUsers(), findAllEventsParamEntity.getStates(), page).stream()
                            .map(EventMapper::mapToEventFullDto)
                            .peek(this::loadStatisticAndRequest)
                            .toList();
                }
                // Юзеры есть, Статус есть, Категории null, Старт null, Конец есть
                return eventRepository.findByInitiatorIdInAndStateInAndEventDateBefore(findAllEventsParamEntity.getUsers(), findAllEventsParamEntity.getStates(), findAllEventsParamEntity.getRangeEnd(), page).stream()
                        .map(EventMapper::mapToEventFullDto)
                        .peek(this::loadStatisticAndRequest)
                        .toList();
            }
            if (findAllEventsParamEntity.getRangeEnd() == null) {
                // Юзеры есть, Статус есть, Категории null, Старт есть, Конец null
                return eventRepository.findByInitiatorIdInAndStateInAndEventDateAfter(findAllEventsParamEntity.getUsers(), findAllEventsParamEntity.getStates(), findAllEventsParamEntity.getRangeStart(), page).stream()
                        .map(EventMapper::mapToEventFullDto)
                        .peek(this::loadStatisticAndRequest)
                        .toList();
            }
            // Юзеры есть, Статус есть, Категории null, Старт есть, Конец есть
            return eventRepository.findByInitiatorIdInAndStateInAndEventDateAfterAndEventDateBefore(findAllEventsParamEntity.getUsers(), findAllEventsParamEntity.getStates(), findAllEventsParamEntity.getRangeStart(), findAllEventsParamEntity.getRangeEnd(), page).stream()
                    .map(EventMapper::mapToEventFullDto)
                    .peek(this::loadStatisticAndRequest)
                    .toList();
        }
        if (findAllEventsParamEntity.getRangeStart() == null) {
            if (findAllEventsParamEntity.getRangeEnd() == null) {
                // Юзеры есть, Статус есть, Категории есть, Старт null, Конец null
                return eventRepository.findByInitiatorIdInAndStateInAndCategoryIdIn(findAllEventsParamEntity.getUsers(), findAllEventsParamEntity.getStates(), findAllEventsParamEntity.getCategories(), page).stream()
                        .map(EventMapper::mapToEventFullDto)
                        .peek(this::loadStatisticAndRequest)
                        .toList();
            }
            // Юзеры есть, Статус есть, Категории есть, Старт null, Конец есть
            return eventRepository.findByInitiatorIdInAndStateInAndCategoryIdInAndEventDateBefore(findAllEventsParamEntity.getUsers(), findAllEventsParamEntity.getStates(), findAllEventsParamEntity.getCategories(), findAllEventsParamEntity.getRangeEnd(), page).stream()
                    .map(EventMapper::mapToEventFullDto)
                    .peek(this::loadStatisticAndRequest)
                    .toList();
        }
        if (findAllEventsParamEntity.getRangeEnd() == null) {
            // Юзеры есть, Статус есть, Категории есть, Старт есть, Конец null
            return eventRepository.findByInitiatorIdInAndStateInAndCategoryIdInAndEventDateAfter(findAllEventsParamEntity.getUsers(), findAllEventsParamEntity.getStates(), findAllEventsParamEntity.getCategories(), findAllEventsParamEntity.getRangeStart(), page).stream()
                    .map(EventMapper::mapToEventFullDto)
                    .peek(this::loadStatisticAndRequest)
                    .toList();
        }
        // Юзеры есть, Статус есть, Категории есть, Старт есть, Конец есть
        return eventRepository.findByInitiatorIdInAndStateInAndCategoryIdInAndEventDateAfterAndEventDateBefore(findAllEventsParamEntity.getUsers(), findAllEventsParamEntity.getStates(), findAllEventsParamEntity.getCategories(), findAllEventsParamEntity.getRangeStart(), findAllEventsParamEntity.getRangeEnd(), page).stream()
                .map(EventMapper::mapToEventFullDto)
                .peek(this::loadStatisticAndRequest)
                .toList();
    }

    @Override
    public EventFullDto editEvent(long eventId, UpdateEventAdminRequestDto updateEventAdminRequestDto) {
        Event event = receiveEvent(eventId);
        if (updateEventAdminRequestDto.getEventDate() != null && updateEventAdminRequestDto.getEventDate().isBefore(event.getCreated().plusHours(1))) {
            throw new ForbiddenException("Date of event cannot be before created date!");
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
                throw new ForbiddenException("Cannot publish the event because it's not in the right state: " + event.getState());
            }
            switch (updateEventAdminRequestDto.getStateAction()) {
                case PUBLISH_EVENT -> { event.setState(State.PUBLISHED); event.setPublished(LocalDateTime.now()); }
                case REJECT_EVENT -> event.setState(State.CANCELED);
            }
        }
        return loadStatisticAndRequest(EventMapper.mapToEventFullDto(eventRepository.save(event)));
    }

    @Override
    public List<EventShortDto> findAllEventsPublic(FindAllEventsPublic findAllEventsPublic) {
        LocalDateTime rangeEnd = findAllEventsPublic.getRangeEnd();
        LocalDateTime rangeStart = findAllEventsPublic.getRangeStart();
        if (rangeEnd != null && rangeStart != null) {
            if (rangeEnd.isBefore(rangeStart)) {
                throw new BadRequestException("'rangeEnd' can not be before 'rangeStart'");
            }
        }
        //TODO Чуть позже допишу этот метод и здесь также нужно будет добавить запросы к репозиторию с запросами.
        return List.of();
    }

    @Override
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
                throw new ForbiddenException("The date and time of event cannot be earlier than two hours from the current moment.");
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

    private EventFullDto loadStatisticAndRequest(EventFullDto event) {
        // TODO Здесь нужно добавить запрос к репозиторию с запросами для заполнения.
        long amountOfViews = statClient.getStat(event.getCreatedOn(), LocalDateTime.now(), List.of("/events/" + event.getId()), true).stream()
                .map(ViewStatsDto::getHits)
                .reduce(0L, Long::sum);
        event.setViews(amountOfViews);
        return event;
    }

    private List<EventFullDto> loadStatisticAndRequestForList(List<EventFullDto> events) {
        // TODO Здесь нужно добавить запрос к репозиторию с запросами для заполнения.
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
        List<ViewStatsDto> viewStats = statClient.getStat(start, LocalDateTime.now(), uris, true);
        return events.stream()
                .peek(event -> event.setViews(viewStats.stream()
                        .filter(view -> view.getUri().equals("/events/" + event.getId()))
                        .map(ViewStatsDto::getHits)
                        .reduce(0L, Long::sum)))
                .toList();
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

    private Location addLocation(LocationDto locationDto) {
        return locationRepository.save(LocationMapper.mapToLocation(locationDto));
    }
}
