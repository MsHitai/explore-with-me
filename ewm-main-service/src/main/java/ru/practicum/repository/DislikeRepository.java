package ru.practicum.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ru.practicum.model.Dislike;

import java.util.List;

@Repository
public interface DislikeRepository extends JpaRepository<Dislike, Long> {

    @Query(value = "SELECT d.id FROM dislikes as d " +
            "LEFT JOIN dislikes_events de on d.id = de.dislike_id " +
            "LEFT JOIN events e on e.id = de.event_id " +
            "WHERE e.id = :eventId", nativeQuery = true)
    List<Dislike> findAllByEventId(Long eventId);

    @Query(value = "SELECT d.id FROM dislikes as d " +
            "LEFT JOIN dislikes_events de on d.id = de.dislike_id " +
            "LEFT OUTER JOIN dislikes_users du on d.id = du.dislike_id " +
            "WHERE du.user_id = :userId and de.event_id = :eventId", nativeQuery = true)
    List<Dislike> findAllByUserIdAndEventId(Long userId, Long eventId);
}
