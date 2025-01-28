package ru.practicum.ewm.main.event.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class NewEventDto {

    private static final String DATE_TIME_PATTERN = "yyyy-MM-dd HH:mm:ss";

    @NotBlank(message = "Field 'annotation' cannot be null, empty or blank")
    @Size(min = 20, max = 2000, message = "Length of field 'annotation' should be in the range from 20 to 2000")
    String annotation;

    @NotNull(message = "Field 'category' is empty")
    Long category;

    @NotBlank(message = "Field 'description' cannot be null, empty or blank")
    @Size(min = 20, max = 7000, message = "Length of field 'description' should be in the range from 20 to 7000")
    String description;

    @NotNull(message = "Field 'eventDate' is empty")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = DATE_TIME_PATTERN)
    LocalDateTime eventDate;

    @NotNull(message = "Field 'location' is empty")
    @Valid
    LocationDto location;

    @NotNull(message = "Field 'paid' is empty")
    Boolean paid = false;

    @NotNull(message = "Field 'participantLimit' is empty")
    @PositiveOrZero(message = "Field 'participantLimit' must be positive or zero")
    Integer participantLimit = 0;

    @NotNull(message = "Field 'requestModeration' is empty")
    Boolean requestModeration = true;

    @NotBlank(message = "Field 'title' cannot be null, empty or blank")
    @Size(min = 3, max = 120, message = "Length of field 'title' should be in the range from 3 to 120")
    String title;
}
