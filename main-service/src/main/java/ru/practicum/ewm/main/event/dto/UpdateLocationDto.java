package ru.practicum.ewm.main.event.dto;

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
public class UpdateLocationDto {

    @PositiveOrZero(message = "Field 'lat' must be positive or zero")
    Float lat;

    @PositiveOrZero(message = "Field 'lon' must be positive or zero")
    Float lon;

    public boolean hasLat() {
        return lat != null;
    }

    public boolean hasLon() {
        return lon != null;
    }
}
