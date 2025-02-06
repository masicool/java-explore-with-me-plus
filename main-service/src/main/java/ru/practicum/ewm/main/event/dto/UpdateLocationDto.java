package ru.practicum.ewm.main.event.dto;

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
    Float lat;
    Float lon;

    public boolean hasLat() {
        return lat != null;
    }

    public boolean hasLon() {
        return lon != null;
    }
}
