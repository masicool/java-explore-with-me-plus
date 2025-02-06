package ru.practicum.ewm.main.event.mapper;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import ru.practicum.ewm.main.event.dto.LocationDto;
import ru.practicum.ewm.main.event.dto.UpdateLocationDto;
import ru.practicum.ewm.main.event.model.Location;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class LocationMapper {
    public static Location mapToLocation(LocationDto dto) {
        Location location = new Location();
        location.setLat(dto.getLat());
        location.setLon(dto.getLon());
        return location;
    }

    public static LocationDto mapToLocationDto(Location location) {
        return new LocationDto(location.getLat(), location.getLon());
    }

    public static Location updateLocationFields(Location location, UpdateLocationDto dto) {
        if (dto.hasLat()) {
            location.setLat(dto.getLat());
        }
        if (dto.hasLon()) {
            location.setLon(dto.getLon());
        }
        return location;
    }
}
