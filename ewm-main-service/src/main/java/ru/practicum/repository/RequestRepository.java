package ru.practicum.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ru.practicum.model.ParticipationRequest;

import java.util.List;

@Repository
public interface RequestRepository extends JpaRepository<ParticipationRequest, Long> {

    List<ParticipationRequest> findAllByEventId(Long eventId);

    ParticipationRequest findByRequesterIdAndEventId(Long requesterId, Long eventId);

    @Query(value = "SELECT r.* FROM requests r " +
            "LEFT OUTER JOIN users u on u.id = r.requester_id " +
            "LEFT OUTER JOIN events e on e.id = r.event_id " +
            "WHERE r.requester_id = ?", nativeQuery = true)
    List<ParticipationRequest> findAllByRequesterId(Long userId);

    List<ParticipationRequest> findAllByIdIn(List<Long> ids);
}
