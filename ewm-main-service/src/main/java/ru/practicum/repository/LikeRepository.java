package ru.practicum.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ru.practicum.model.Like;

import java.util.List;

@Repository
public interface LikeRepository extends JpaRepository<Like, Long> {

    @Query(value = "SELECT l.id FROM likes as l " +
            "LEFT JOIN likes_events le on l.id = le.like_id " +
            "LEFT JOIN events e on e.id = le.event_id " +
            "WHERE e.id = :eventId", nativeQuery = true)
    List<Like> findAllByEventId(Long eventId);

    @Query(value = "SELECT l.id FROM likes l " +
            "LEFT JOIN likes_users lu on l.id = lu.like_id " +
            "LEFT JOIN likes_events le on l.id = le.like_id " +
            "WHERE lu.user_id = :userId AND le.event_id = :eventId", nativeQuery = true)
    List<Like> findAllByUserIdAndEventId(Long userId, Long eventId);
}
