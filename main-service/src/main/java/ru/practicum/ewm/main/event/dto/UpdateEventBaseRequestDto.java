package ru.practicum.ewm.main.event.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.Valid;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public abstract class UpdateEventBaseRequestDto {
    private static final String DATE_TIME_PATTERN = "yyyy-MM-dd HH:mm:ss";

    @Size(min = 20, max = 2000, message = "Length of field 'annotation' should be in the range from 20 to 2000")
    String annotation;

    Long category;

    @Size(min = 20, max = 7000, message = "Length of field 'description' should be in the range from 20 to 7000")
    String description;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = DATE_TIME_PATTERN)
    LocalDateTime eventDate;

    @Valid
    UpdateLocationDto location;

    Boolean paid;

    @PositiveOrZero(message = "Field 'participantLimit' must be positive or zero")
    Integer participantLimit;

    Boolean requestModeration;

    @Size(min = 3, max = 120, message = "Length of field 'title' should be in the range from 3 to 120")
    String title;

}
