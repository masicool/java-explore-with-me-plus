package ru.practicum.ewm.main.event.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class UpdateEventFieldsEntity {
    String annotation;
    Long categoryId;
    String description;
    LocalDateTime eventDate;
    UpdateLocationDto location; // TODO Под фичу: пусть пользователь кидает id локации, если локация новая - пусть сначала добавит ее, пока что так оставим.
    Boolean paid;
    Integer participantLimit;
    Boolean requestModeration;
    String title;

    public boolean hasAnnotation() {
        return !(annotation == null || annotation.isBlank());
    }

    public boolean hasCategoryId() {
        return categoryId != null;
    }

    public boolean hasDescription() {
        return !(description == null || description.isBlank());
    }

    public boolean hasEventDate() {
        return eventDate != null;
    }

    public boolean hasLocation() {
        return location != null;
    }

    public boolean hasPaid() {
        return paid != null;
    }

    public boolean hasParticipantLimit() {
        return participantLimit != null;
    }

    public boolean hasRequestModeration() {
        return requestModeration != null;
    }

    public boolean hasTitle() {
        return !(title == null || title.isBlank());
    }
}
