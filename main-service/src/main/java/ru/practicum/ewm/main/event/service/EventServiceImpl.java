package ru.practicum.ewm.main.event.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.main.category.model.Category;
import ru.practicum.ewm.main.category.repository.CategoryRepository;
import ru.practicum.ewm.main.event.dto.EventFullDto;
import ru.practicum.ewm.main.event.dto.NewEventDto;
import ru.practicum.ewm.main.event.dto.UpdateEventUserRequest;
import ru.practicum.ewm.main.event.model.Event;
import ru.practicum.ewm.main.event.model.State;
import ru.practicum.ewm.main.event.repository.EventRepository;
import ru.practicum.ewm.main.exception.type.BadRequestException;
import ru.practicum.ewm.main.exception.type.NotFoundException;
import ru.practicum.ewm.main.user.model.User;
import ru.practicum.ewm.main.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Transactional
public class EventServiceImpl implements EventService {
    final EventRepository eventRepository;
    final UserRepository userRepository;
    final CategoryRepository categoryRepository;
    final ModelMapper modelMapper;

    @Override
    public EventFullDto addEvent(long userId, NewEventDto newEventDto) {
        User user = findUser(userId);
        Category category = findCategory(newEventDto.getCategory());
        Event event = modelMapper.map(newEventDto, Event.class);
        event.setInitiator(user);
        event.setCategory(category);
        event.setState(State.PENDING);
        event.setCreated(LocalDateTime.now());
        return modelMapper.map(eventRepository.save(event), EventFullDto.class);
    }

    @Override
    public EventFullDto updateEvent(long userId, Long eventId, UpdateEventUserRequest updateEventUserRequest) {
        User user = findUser(userId);
        Event event = findEvent(eventId);

        checkValidUserForEvent(user, event);

        if (event.getState() == State.PUBLISHED)
            throw new BadRequestException("Event must not be published");

        updateFields(event, updateEventUserRequest);

        return modelMapper.map(eventRepository.save(event), EventFullDto.class);
    }

    @Override
    public EventFullDto findOwnersEventById(long userId, long eventId) {
        User user = findUser(userId);
        Event event = findEvent(eventId);

        checkValidUserForEvent(user, event);

        return modelMapper.map(eventRepository.findById(eventId), EventFullDto.class);
    }

    @Override
    public List<EventFullDto> findOwnersEvents(long userId, int from, int size) {
        User user = findUser(userId);
        PageRequest page = PageRequest.of(from > 0 ? from / size : 0, size);
        return eventRepository.findAllByInitiatorId(user.getId(), page).stream()
                .map(o -> modelMapper.map(o, EventFullDto.class))
                .toList();
    }

    private void updateFields(Event event, UpdateEventUserRequest changes) {
        if (changes.getPaid() != null) event.setPaid(changes.getPaid());
        if (changes.getRequestModeration() != null) event.setRequestModeration(changes.getRequestModeration());
        if (changes.getAnnotation() != null) event.setAnnotation(changes.getAnnotation());

        if (changes.getCategory() != null) {
            Category category = findCategory(changes.getCategory());
            event.setCategory(category);
        }

        if (changes.getDescription() != null) event.setDescription(changes.getDescription());

        if (changes.getEventDate() != null) {
            if (changes.getEventDate().isBefore(LocalDateTime.now().plusHours(2)))
                throw new BadRequestException("The date and time cannot be earlier than two hours from the current moment.");
            event.setEventDate(changes.getEventDate());
        }

        if (changes.getLocation() != null) {
            event.setLat(changes.getLocation().getLat());
            event.setLon(changes.getLocation().getLon());
        }

        if (changes.getStateAction() != null) {
            switch (changes.getStateAction()) {
                case SEND_TO_REVIEW -> event.setState(State.PENDING);
                case CANCEL_REVIEW -> event.setState(State.CANCELED);
            }
        }

        if (changes.getTitle() != null) event.setTitle(changes.getTitle());
        if (changes.getParticipantLimit() != null) event.setParticipantLimit(changes.getParticipantLimit());
    }

    private User findUser(long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User with id=" + userId + " was not found"));
    }

    private Category findCategory(long catId) {
        return categoryRepository.findById(catId)
                .orElseThrow(() -> new NotFoundException("Category with id=" + catId + " was not found"));
    }

    private Event findEvent(long eventId) {
        return eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Event with id=" + eventId + " was not found"));
    }

    private void checkValidUserForEvent(User user, Event event) {
        if (!event.getInitiator().equals(user))
            throw new BadRequestException("Event is not for this user");
    }
}
