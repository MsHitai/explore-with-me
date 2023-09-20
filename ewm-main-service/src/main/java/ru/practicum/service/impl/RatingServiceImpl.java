package ru.practicum.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import ru.practicum.dto.EventShortRatingDto;
import ru.practicum.dto.SearchEventParams;
import ru.practicum.dto.UserShortRatingDto;
import ru.practicum.exception.DataConflictException;
import ru.practicum.exception.DataNotFoundException;
import ru.practicum.exception.ValidationException;
import ru.practicum.mapper.EventMapper;
import ru.practicum.model.*;
import ru.practicum.repository.*;
import ru.practicum.service.RatingService;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RatingServiceImpl implements RatingService {

    private final LikeRepository likeRepository;
    private final DislikeRepository dislikeRepository;
    private final UserRepository userRepository;
    private final EventRepository eventRepository;
    private final RatingRepository ratingRepository;

    @Override
    public EventShortRatingDto addLike(Long userId, Long eventId) {
        User user = checkUserId(userId);
        Event event = checkEventId(eventId);

        checkEventAndUser(user, event);
        List<Like> likeDuplicates = likeRepository.findAllByUserIdAndEventId(user.getId(), event.getId());
        if (likeDuplicates.size() > 0) {
            throw new DataConflictException("This user has already liked the event");
        }

        Like like = new Like();
        like.setEvents(new ArrayList<>());
        like.setUsers(new ArrayList<>());
        like.getUsers().add(user);
        like.getEvents().add(event);
        likeRepository.save(like);

        double rating = calculateEventRating(event);

        return EventMapper.mapToShortRatingDto(event, rating);
    }

    @Override
    public EventShortRatingDto addDislike(Long userId, Long eventId) {
        User user = checkUserId(userId);
        Event event = checkEventId(eventId);

        checkEventAndUser(user, event);
        List<Dislike> disDuplicates = dislikeRepository.findAllByUserIdAndEventId(user.getId(), event.getId());
        if (disDuplicates.size() > 0) {
            throw new DataConflictException("This user has already disliked the event");
        }

        Dislike dislike = new Dislike();
        dislike.setEvents(new ArrayList<>());
        dislike.setUsers(new ArrayList<>());
        dislike.getEvents().add(event);
        dislike.getUsers().add(user);
        dislikeRepository.save(dislike);

        double rating = calculateEventRating(event);

        return EventMapper.mapToShortRatingDto(event, rating);
    }

    @Override
    public List<EventShortRatingDto> findEventsWithRating(SearchEventParams params, Pageable page) {
        LocalDateTime rangeStart = params.getRangeStart();
        LocalDateTime rangeEnd = params.getRangeEnd();
        if (rangeStart == null) {
            rangeStart = LocalDateTime.now();
        }
        if (rangeEnd == null) {
            rangeEnd = LocalDateTime.now().plusYears(100);
        }
        if (rangeStart.isAfter(rangeEnd)) {
            throw new ValidationException("The start of the event cannot be after the end of the event");
        }
        List<Event> events = eventRepository.findEventsForPublic(params.getText(), params.getCategories(),
                params.getPaid(), rangeStart, rangeEnd, params.getOnlyAvailable(), page);

        List<EventShortRatingDto> result = new ArrayList<>();
        List<Rating> ratings = ratingRepository.findAllByEventIn(events);

        for (Event event : events) {
            for (Rating rating : ratings) {
                if (rating.getEvent().equals(event)) {
                    result.add(EventMapper.mapToShortRatingDto(event, rating.getEventStars()));
                } else {
                    result.add(EventMapper.mapToShortRatingDto(event, 0.00));
                }
            }
        }

        return result.stream()
                .sorted(Comparator.comparing(EventShortRatingDto::getRating).reversed())
                .collect(Collectors.toList());
    }

    @Override
    public List<UserShortRatingDto> findMostPopularUsers(Pageable page) {
        return ratingRepository.findAllWithRating(page);
    }

    private double calculateUserRating(Long eventId) {
        double sumEventStars = 0.00;
        List<Rating> ratings = ratingRepository.findAllByEventId(eventId);
        for (Rating rating : ratings) {
            sumEventStars += rating.getEventStars();
        }
        double totalSuchEvents = ratings.size();// для вычисления среднего арифметического

        return sumEventStars / totalSuchEvents;
    }

    private double calculateEventRating(Event event) {
        List<Like> likes = likeRepository.findAllByEventId(event.getId());
        List<Dislike> dislikes = dislikeRepository.findAllByEventId(event.getId());
        Rating existingRating = ratingRepository.findByEventAndUser(event, event.getInitiator());
        double likesNum = likes.size();
        double dislikesNum = dislikes.size();
        double result = (likesNum / (likesNum + dislikesNum)) * likesNum;
        result = Math.min(result, 5.00);

        if (existingRating != null) {
            existingRating.setEventStars(result);
            ratingRepository.save(existingRating);
            double userStars = Math.min(calculateUserRating(event.getId()), 5.00);
            existingRating.setUserStars(userStars);
            ratingRepository.save(existingRating);
        } else {
            Rating rating = new Rating();
            rating.setEventStars(result);
            rating.setEvent(event);
            rating.setUser(event.getInitiator());
            ratingRepository.save(rating);
        }
        return result;
    }

    //каждый может ставить (диз)лайк событию, кроме инициатора, т.к. есть события без лимита и модерации, плюс это
    //показатель (не)заинтересованности на будущее для организаторов. Дубликаты не принимаются.
    private void checkEventAndUser(User user, Event event) {
        if (!event.getState().equals(EventState.PUBLISHED)) {
            throw new DataConflictException("The event hasn't been published yet");
        }
        if (event.getInitiator().equals(user)) {
            throw new DataConflictException("The initiator cannot rate their event");
        }
    }

    private User checkUserId(Long userId) {
        return userRepository.findById(userId).orElseThrow(() -> new DataNotFoundException(
                String.format("User with the id=%d was not found in the database", userId)));
    }

    private Event checkEventId(Long eventId) {
        return eventRepository.findByIdWithAllFields(eventId).orElseThrow(() -> new DataNotFoundException(
                String.format("Event with the id=%d was not found in the database", eventId)
        ));
    }
}
