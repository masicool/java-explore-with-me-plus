package ru.practicum.ewm.main.event.mapper;

import ru.practicum.ewm.main.event.dto.CommentFullDto;
import ru.practicum.ewm.main.event.dto.NewCommentDto;
import ru.practicum.ewm.main.event.model.Comment;
import ru.practicum.ewm.main.event.model.Event;
import ru.practicum.ewm.main.user.model.User;

import java.util.ArrayList;
import java.util.List;

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
        commentFullDto.setCreated(comment.getCreated());
        commentFullDto.setLastUpdate(comment.getLastUpdate());
        return commentFullDto;
    }

    public static List<CommentFullDto> mapToCommentFullDto(List<Comment> comments) {
        List<CommentFullDto> dtos = new ArrayList<>();
        comments.forEach(comment -> dtos.add(mapToCommentFullDto(comment)));
        return dtos;
    }
}
