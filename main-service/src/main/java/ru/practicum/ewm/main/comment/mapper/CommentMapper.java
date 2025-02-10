package ru.practicum.ewm.main.comment.mapper;

import ru.practicum.ewm.main.comment.dto.CommentFullDto;
import ru.practicum.ewm.main.comment.dto.NewCommentDto;
import ru.practicum.ewm.main.comment.model.Comment;
import ru.practicum.ewm.main.event.model.Event;
import ru.practicum.ewm.main.user.UserMapper;
import ru.practicum.ewm.main.user.model.User;

public class CommentMapper {
    public static Comment mapToComment(Event event, User user, NewCommentDto newCommentDto) {
        Comment comment = new Comment();
        comment.setText(newCommentDto.getText());
        comment.setAuthor(user);
        comment.setEvent(event);
        return comment;
    }

    public static CommentFullDto mapToCommentFullDto(Comment comment) {
        CommentFullDto commentFullDto = new CommentFullDto();
        commentFullDto.setId(comment.getId());
        commentFullDto.setText(comment.getText());
        commentFullDto.setAuthor(UserMapper.mapToUserShortDto(comment.getAuthor()));
        commentFullDto.setCreated(comment.getCreated());
        commentFullDto.setLastUpdate(comment.getLastUpdate());
        return commentFullDto;
    }
}
