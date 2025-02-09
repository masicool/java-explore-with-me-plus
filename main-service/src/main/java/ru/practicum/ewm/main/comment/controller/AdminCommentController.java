package ru.practicum.ewm.main.comment.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.main.comment.dto.CommentFullDto;
import ru.practicum.ewm.main.comment.dto.UpdateCommentDto;
import ru.practicum.ewm.main.comment.service.CommentService;

@RestController
@RequestMapping("/admin/comments")
@RequiredArgsConstructor
public class AdminCommentController {

    private final CommentService commentService;

    @PatchMapping("/{commentId}")
    @ResponseStatus(HttpStatus.OK)
    public CommentFullDto updateComment(@PathVariable long commentId,
                                        @Valid @RequestBody UpdateCommentDto updateCommentDto) {
        return commentService.updateCommentAdmin(commentId, updateCommentDto);
    }

    @DeleteMapping("/{commentId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteComment(@PathVariable long commentId) {
        commentService.deleteCommentAdmin(commentId);
    }

    @DeleteMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteAllEventComments(@RequestParam long eventId) {
        commentService.deleteAllEventCommentsAdmin(eventId);
    }
}
