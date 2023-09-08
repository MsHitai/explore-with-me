package ru.practicum.mapper;

import lombok.experimental.UtilityClass;
import ru.practicum.dto.LocationDto;
import ru.practicum.model.Location;

@UtilityClass
public class LocationMapper {

    public Location mapToLocation(LocationDto dto) {
        return Location.builder()
                .lat(dto.getLat())
                .lon(dto.getLon())
                .build();
    }

    public LocationDto mapToDto(Location location) {
        return new LocationDto(
                location.getLat(),
                location.getLon()
        );
    }
}
