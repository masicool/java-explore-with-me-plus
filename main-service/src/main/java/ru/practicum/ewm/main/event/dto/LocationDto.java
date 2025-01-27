package ru.practicum.ewm.main.event.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Data
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class LocationDto {

    @NotNull(message = "Field 'lat' is empty")
    @PositiveOrZero(message = "Field 'lat' must be positive or zero")
    Float lat;

    @NotNull(message = "Field 'lon' is empty")
    @PositiveOrZero(message = "Field 'lon' must be positive or zero")
    Float lon;
}
