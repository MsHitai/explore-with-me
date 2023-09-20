package ru.practicum.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;
import ru.practicum.dto.EventShortRatingDto;
import ru.practicum.dto.SearchEventParams;
import ru.practicum.dto.UserShortRatingDto;
import ru.practicum.service.RatingService;

import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/ratings")
@Slf4j
public class RatingController {

    private final RatingService ratingService;

    @Autowired
    public RatingController(RatingService ratingService) {
        this.ratingService = ratingService;
    }

    @PostMapping("/{userId}/likes")
    public EventShortRatingDto addLike(@PathVariable Long userId, @RequestParam Long eventId) {
        log.info("Received POST request to add a like from user {} to the event {}", userId, eventId);
        return ratingService.addLike(userId, eventId);
    }

    @PostMapping("/{userId}/dislikes")
    public EventShortRatingDto addDislike(@PathVariable Long userId, @RequestParam Long eventId) {
        log.info("Received POST request to add a dislike from user {} to the event {}", userId, eventId);
        return ratingService.addDislike(userId, eventId);
    }

    @GetMapping("/events")
    public List<EventShortRatingDto> findEventsWithRating(@PositiveOrZero @RequestParam(defaultValue = "0") Integer from,
                                                          @Positive @RequestParam(defaultValue = "10") Integer size,
                                                          @RequestParam(required = false) Boolean paid,
                                                          @RequestParam(required = false)
                                                          @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
                                                          LocalDateTime rangeStart,
                                                          @RequestParam(required = false)
                                                          @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
                                                          LocalDateTime rangeEnd,
                                                          @RequestParam(required = false) Boolean onlyAvailable,
                                                          @RequestParam(required = false) String text,
                                                          @RequestParam(required = false) List<Integer> categories) {
        log.info("Received GET request to find all events with rating with these params: from {}, size {}, paid {}, " +
                        "rangeStart {}, rangeEnd {}, onlyAvailable {}, text {}, categories {} ", from, size, paid,
                rangeStart, rangeEnd, onlyAvailable, text, categories);
        Pageable page = PageRequest.of(from / size, size);
        SearchEventParams params = SearchEventParams.builder()
                .text(text)
                .categories(categories)
                .paid(paid)
                .rangeStart(rangeStart)
                .rangeEnd(rangeEnd)
                .onlyAvailable(onlyAvailable)
                .build();
        return ratingService.findEventsWithRating(params, page);
    }

    @GetMapping("/users")
    public List<UserShortRatingDto> findMostPopularUsers(@PositiveOrZero @RequestParam(defaultValue = "0") Integer from,
                                                         @Positive @RequestParam(defaultValue = "10") Integer size) {
        log.info("Received GET request to find all users with rating from {} to {}", from, size);
        Pageable page = PageRequest.of(from / size, size);
        return ratingService.findMostPopularUsers(page);
    }
}
