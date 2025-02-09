package ru.practicum.ewm.main.comment.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.main.comment.dto.CommentFullDto;
import ru.practicum.ewm.main.comment.dto.NewCommentDto;
import ru.practicum.ewm.main.comment.dto.UpdateCommentDto;
import ru.practicum.ewm.main.comment.mapper.CommentMapper;
import ru.practicum.ewm.main.comment.model.Comment;
import ru.practicum.ewm.main.comment.repository.CommentRepository;
import ru.practicum.ewm.main.event.model.Event;
import ru.practicum.ewm.main.event.model.State;
import ru.practicum.ewm.main.event.repository.EventRepository;
import ru.practicum.ewm.main.exception.type.BadRequestException;
import ru.practicum.ewm.main.exception.type.NotFoundException;
import ru.practicum.ewm.main.request.model.Status;
import ru.practicum.ewm.main.request.repository.RequestRepository;
import ru.practicum.ewm.main.user.model.User;
import ru.practicum.ewm.main.user.repository.UserRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Transactional
public class CommentServiceImpl implements CommentService {

    CommentRepository commentRepository;
    UserRepository userRepository;
    EventRepository eventRepository;
    RequestRepository requestRepository;

    @Override
    public CommentFullDto addComment(long userId, long eventId, NewCommentDto newCommentDto) {
        User user = receiveUser(userId);
        Event event = receiveEvent(eventId);
        checkValidEventStatusAndRequester(event, user);
        return CommentMapper.mapToCommentFullDto(commentRepository.save(CommentMapper.mapToComment(event, user, newCommentDto)));
    }

    @Override
    public CommentFullDto updateComment(long userId, long eventId, long commentId, UpdateCommentDto updateCommentDto) {
        User user = receiveUser(userId);
        Event event = receiveEvent(eventId);
        Comment comment = receiveComment(commentId);
        checkValidRequester(event, user, comment);
        comment.setText(updateCommentDto.getText());
        return CommentMapper.mapToCommentFullDto(commentRepository.save(comment));
    }

    @Override
    public CommentFullDto updateCommentAdmin(long commentId, UpdateCommentDto updateCommentDto) {
        Comment comment = receiveComment(commentId);
        comment.setText(updateCommentDto.getText());
        return CommentMapper.mapToCommentFullDto(commentRepository.save(comment));
    }

    @Override
    @Transactional(readOnly = true)
    public CommentFullDto findComment(long commentId) {
        return CommentMapper.mapToCommentFullDto(receiveComment(commentId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<CommentFullDto> findAllEventComments(long eventId, int from, int size) {
        PageRequest page = PageRequest.of(from, size);
        return commentRepository.findAllByEventId(eventId, page).stream().map(CommentMapper::mapToCommentFullDto).toList();
    }

    @Override
    public void deleteCommentAdmin(long commentId) {
        commentRepository.deleteById(commentId);
    }

    @Override
    public void deleteComment(long userId, long eventId, long commentId) {
        User user = receiveUser(userId);
        Event event = receiveEvent(eventId);
        Comment comment = receiveComment(commentId);
        checkValidRequester(event, user, comment);
        commentRepository.deleteById(commentId);
    }

    @Override
    public void deleteAllEventCommentsAdmin(long eventId) {
        commentRepository.deleteAllByEventId(eventId);
    }

    private void checkValidEventStatusAndRequester(Event event, User user) {
        if (event.getState() != State.PUBLISHED) {
            throw new BadRequestException("Event with id=" + event.getId() + " must be published");
        }
        if (event.isRequestModeration() || event.getParticipantLimit() != 0) {
            if (!event.getInitiator().equals(user) && requestRepository.findByRequesterIdAndEventId(user.getId(), event.getId())
                    .filter(o -> o.getStatus() == Status.CONFIRMED).isEmpty()) {
                throw new BadRequestException("User with id=" + user.getId() + " cannot work with comments");
            }
        }
    }

    private void checkValidRequester(Event event, User user, Comment comment) {
        if (!comment.getAuthor().equals(user) && !event.getInitiator().equals(user)) {
            throw new BadRequestException("User " + user.getId() + " cannot delete a comment " + comment.getId() + " that is not his own.");
        }
    }

    private Comment receiveComment(long commentId) {
        return commentRepository.findById(commentId)
                .orElseThrow(() -> new NotFoundException("Comment with id=" + commentId + " was not found"));
    }

    private User receiveUser(long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User with id=" + userId + " was not found"));
    }

    private Event receiveEvent(long eventId) {
        return eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Event with id=" + eventId + " was not found"));
    }
}
