package ru.practicum.ewm.event;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.main.category.model.Category;
import ru.practicum.ewm.main.event.dto.CommentFullDto;
import ru.practicum.ewm.main.event.dto.NewCommentDto;
import ru.practicum.ewm.main.event.dto.UpdateCommentDto;
import ru.practicum.ewm.main.event.mapper.CommentMapper;
import ru.practicum.ewm.main.event.model.Comment;
import ru.practicum.ewm.main.event.model.Event;
import ru.practicum.ewm.main.event.model.Location;
import ru.practicum.ewm.main.event.model.State;
import ru.practicum.ewm.main.event.service.EventService;
import ru.practicum.ewm.main.exception.type.BadRequestException;
import ru.practicum.ewm.main.exception.type.NotFoundException;
import ru.practicum.ewm.main.user.model.User;

import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.junit.jupiter.api.Assertions.assertThrows;

@Transactional
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@SpringBootTest
@FieldDefaults(level = AccessLevel.PRIVATE)
public class EventServiceIntegrationTest {
    final EntityManager em;
    final EventService eventService;

    User user1;
    User user2;
    Event event1;
    Comment comment1;

    @BeforeEach
    void beforeEach() {
        user1 = new User();
        user1.setName("user1");
        user1.setEmail("user1@mail.ru");
        em.persist(user1);
        em.flush();

        user2 = new User();
        user2.setName("user2");
        user2.setEmail("user2@mail.ru");
        em.persist(user2);
        em.flush();

        Category category = new Category();
        category.setName("category1");
        em.persist(category);
        em.flush();

        Location location = new Location();
        location.setLat(0);
        location.setLon(0);
        em.persist(location);
        em.flush();

        event1 = new Event();
        event1.setInitiator(user1);
        event1.setTitle("title1");
        event1.setState(State.PUBLISHED);
        event1.setEventDate(LocalDateTime.now().plusDays(2));
        event1.setCategory(category);
        event1.setDescription("description1");
        event1.setAnnotation("annotation1");
        event1.setLocation(location);
        event1.setCreated(LocalDateTime.now().withNano(0));
        em.persist(event1);
        em.flush();

        comment1 = new Comment();
        comment1.setEvent(event1);
        comment1.setAuthor(user1);
        comment1.setText("comment1");
    }

    @Test
    void addCommentTest() {
        NewCommentDto newCommentDto = new NewCommentDto();
        newCommentDto.setText("comment1");
        CommentFullDto commentFullDto = eventService.addComment(user1.getId(), event1.getId(), newCommentDto);

        TypedQuery<Comment> query = em.createQuery("from Comment ", Comment.class);
        List<CommentFullDto> commentFullDtos = query.getResultList().stream().map(CommentMapper::mapToCommentFullDto).toList();

        assertThat(commentFullDtos.size(), equalTo(1));
        assertThat(commentFullDtos, hasItem(commentFullDto));

        commentFullDto = commentFullDtos.getFirst();
        assertThat(commentFullDto.getId(), notNullValue());
        assertThat(commentFullDto.getCreated(), notNullValue());
        assertThat(commentFullDto.getLastUpdate(), notNullValue());
    }

    @Test
    void updateCommentTest() {
        em.persist(comment1);
        em.flush();

        UpdateCommentDto updateCommentDto = new UpdateCommentDto();
        updateCommentDto.setText("comment1_updated");
        eventService.updateComment(user1.getId(), event1.getId(), comment1.getId(), updateCommentDto);

        CommentFullDto commentFullDto = eventService.findComment(comment1.getId());
        assertThat(commentFullDto.getText(), equalTo(updateCommentDto.getText()));
    }

    @Test
    void updateCommentAdminTest() {
        em.persist(comment1);
        em.flush();

        UpdateCommentDto updateCommentDto = new UpdateCommentDto();
        updateCommentDto.setText("comment1_updated");
        eventService.updateCommentAdmin(comment1.getId(), updateCommentDto);

        CommentFullDto commentFullDto = eventService.findComment(comment1.getId());
        assertThat(commentFullDto.getText(), equalTo(updateCommentDto.getText()));
    }

    @Test
    void findAllEventCommentsTest() {
        em.persist(comment1);
        em.flush();

        Comment comment2 = new Comment();
        comment2.setEvent(event1);
        comment2.setAuthor(user1);
        comment2.setText("comment2");
        em.persist(comment2);
        em.flush();

        Comment comment3 = new Comment();
        comment3.setEvent(event1);
        comment3.setAuthor(user1);
        comment3.setText("comment3");
        em.persist(comment3);
        em.flush();

        List<CommentFullDto> commentFullDtos = eventService.findAllEventComments(event1.getId(), 0, 100);

        assertThat(commentFullDtos.size(), equalTo(3));
        assertThat(commentFullDtos, hasItem(CommentMapper.mapToCommentFullDto(comment1)));
        assertThat(commentFullDtos, hasItem(CommentMapper.mapToCommentFullDto(comment2)));
        assertThat(commentFullDtos, hasItem(CommentMapper.mapToCommentFullDto(comment3)));
    }

    @Test
    void deleteComment() {
        em.persist(comment1);
        em.flush();

        // удаление чужого комментария
        assertThrows(BadRequestException.class, () -> eventService.deleteComment(user2.getId(), event1.getId(), comment1.getId()));

        // удаление не существующего комментария
        assertThrows(NotFoundException.class, () -> eventService.deleteComment(user2.getId(), event1.getId(), Long.MAX_VALUE));

        // удаление комментария
        eventService.deleteComment(user1.getId(), event1.getId(), comment1.getId());
        List<CommentFullDto> commentFullDtos = eventService.findAllEventComments(event1.getId(), 0, 100);
        assertThat(commentFullDtos.size(), equalTo(0));
    }
}
