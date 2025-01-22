package ru.practicum.ewm.main.event.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;
import ru.practicum.ewm.main.event.model.Location;
import ru.practicum.ewm.main.event.model.StateAction;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UpdateEventUserRequest {
    @Size(min = 20, max = 2000, message = "Length of field 'annotation' should be in the range from 20 to 2000")
    String annotation;

    Long category;

    @Size(min = 20, max = 7000, message = "Length of field 'description' should be in the range from 20 to 7000")
    String description;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    LocalDateTime eventDate;

    Location location;
    Boolean paid;

    @Min(value = 0, message = "Field 'participantLimit' must be positive")
    Integer participantLimit;

    Boolean requestModeration;
    StateAction stateAction;

    @Size(min = 3, max = 120, message = "Length of field 'title' should be in the range from 3 to 120")
    String title;
}
