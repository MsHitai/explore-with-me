package ru.practicum.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ru.practicum.dto.UserShortRatingDto;
import ru.practicum.model.Event;
import ru.practicum.model.Rating;
import ru.practicum.model.User;

import java.util.List;

@Repository
public interface RatingRepository extends JpaRepository<Rating, Long> {
    Rating findByEventAndUser(Event event, User initiator);

    List<Rating> findAllByEventId(Long eventId);

    List<Rating> findAllByEventIn(List<Event> events);

    @Query("select new ru.practicum.dto.UserShortRatingDto(r.user.id, r.user.name, r.userStars) " +
            "from Rating r " +
            "order by r.userStars desc ")
    List<UserShortRatingDto> findAllWithRating(Pageable page);
}
