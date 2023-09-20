package ru.practicum.service;

import org.springframework.data.domain.Pageable;
import ru.practicum.dto.EventShortRatingDto;
import ru.practicum.dto.SearchEventParams;
import ru.practicum.dto.UserShortRatingDto;

import java.util.List;

public interface RatingService {
    EventShortRatingDto addLike(Long userId, Long eventId);

    EventShortRatingDto addDislike(Long userId, Long eventId);

    List<EventShortRatingDto> findEventsWithRating(SearchEventParams params, Pageable page);

    List<UserShortRatingDto> findMostPopularUsers(Pageable page);

}
