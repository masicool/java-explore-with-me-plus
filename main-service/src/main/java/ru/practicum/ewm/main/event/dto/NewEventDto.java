package ru.practicum.ewm.main.event.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;
import ru.practicum.ewm.main.event.dto.validator.DateTimeValidAnnotation;
import ru.practicum.ewm.main.event.model.Location;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@DateTimeValidAnnotation
public class NewEventDto {
    @NotBlank(message = "Field 'annotation' cannot be null, empty or blank")
    @Size(min = 20, max = 2000, message = "Length of field 'annotation' should be in the range from 20 to 2000")
    String annotation;

    @NotNull(message = "Field 'category' is empty")
    Long category;

    @NotBlank(message = "Field 'description' cannot be null, empty or blank")
    @Size(min = 20, max = 7000, message = "Length of field 'description' should be in the range from 20 to 7000")
    String description;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    LocalDateTime eventDate;

    Location location;

    Boolean paid = false;

    @Min(value = 0, message = "Field 'participantLimit' must be positive")
    Integer participantLimit = 0;

    Boolean requestModeration = true;

    @NotBlank(message = "Field 'title' cannot be null, empty or blank")
    @Size(min = 3, max = 120, message = "Length of field 'title' should be in the range from 3 to 120")
    String title;
}
