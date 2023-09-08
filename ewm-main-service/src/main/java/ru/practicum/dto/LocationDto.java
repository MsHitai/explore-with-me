package ru.practicum.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class LocationDto {

    private double lat;
    private double lon;
}
