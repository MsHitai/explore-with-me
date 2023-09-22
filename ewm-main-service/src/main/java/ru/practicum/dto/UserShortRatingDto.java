package ru.practicum.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserShortRatingDto {

    private Long userId;
    private String name;
    private double userStars;

    public UserShortRatingDto(Long userId, String name, double userStars) {
        this.userId = userId;
        this.name = name;
        this.userStars = userStars;
    }
}
