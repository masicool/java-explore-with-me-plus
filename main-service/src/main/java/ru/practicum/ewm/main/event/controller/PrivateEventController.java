package ru.practicum.ewm.main.event.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.main.event.dto.*;
import ru.practicum.ewm.main.event.service.EventService;

import java.util.List;

@RestController
@RequestMapping("/users/{userId}/events")
@RequiredArgsConstructor
public class PrivateEventController {
    private final EventService eventService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public EventFullDto addEvent(@PathVariable long userId,
                                 @Valid @RequestBody NewEventDto newEventDto) {
        return eventService.addEvent(userId, newEventDto);
    }

    @PatchMapping("/{eventId}")
    @ResponseStatus(HttpStatus.OK)
    public EventFullDto updateEvent(@PathVariable long userId,
                                    @PathVariable long eventId,
                                    @Valid @RequestBody UpdateEventUserRequestDto updateEventUserRequestDto) {
        return eventService.updateEvent(userId, eventId, updateEventUserRequestDto);
    }

    @GetMapping("/{eventId}")
    @ResponseStatus(HttpStatus.OK)
    public EventFullDto findOwnersEventById(@PathVariable long userId,
                                            @PathVariable long eventId) {
        return eventService.findOwnersEventById(userId, eventId);
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<EventFullDto> findOwnersEvents(@PathVariable long userId,
                                               @RequestParam(defaultValue = "0") @PositiveOrZero int from,
                                               @RequestParam(defaultValue = "10") @Positive int size) {
        return eventService.findOwnersEvents(userId, from, size);
    }

    @PostMapping("/{eventId}/comment")
    @ResponseStatus(HttpStatus.CREATED)
    public CommentFullDto addComment(@PathVariable long userId,
                                     @PathVariable long eventId,
                                     @Valid @RequestParam NewCommentDto newCommentDto) {
        return eventService.addComment(userId, eventId, newCommentDto);
    }

    @PatchMapping("/{eventId}/comment/{commentId}")
    @ResponseStatus(HttpStatus.OK)
    public CommentFullDto updateComment(@PathVariable long userId,
                                        @PathVariable long eventId,
                                        @PathVariable long commentId,
                                        @Valid @RequestParam UpdateCommentDto updateCommentDto) {
        return eventService.updateComment(userId, eventId, commentId, updateCommentDto);
    }

    @DeleteMapping("/{eventId}/comment/{commentId}")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public void deleteComment(@PathVariable long userId,
                              @PathVariable long eventId,
                              @PathVariable long commentId) {
        eventService.deleteComment(userId, eventId, commentId);
    }
}
