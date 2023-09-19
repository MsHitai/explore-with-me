package ru.practicum.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ru.practicum.model.Event;
import ru.practicum.model.EventState;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface EventRepository extends JpaRepository<Event, Long> {


    List<Event> findAllByInitiatorId(Long initiatorId, Pageable page);

    @Query(value = "SELECT e.* FROM events e " +
            "LEFT JOIN categories c ON e.category_id = c.id " +
            "LEFT JOIN locations l ON e.location_id = l.id " +
            "WHERE e.id = ? AND e.initiator_id = ?", nativeQuery = true)
    Event findByIdAndInitiatorId(Long eventId, Long initiatorId);

    @Query("SELECT e from Event e " +
            "WHERE (:users is null or e.initiator.id in :users) " +
            "AND (:states is null or e.state in :states) " +
            "AND (:categories is null or e.category.id in :categories) " +
            "AND e.eventDate > :rangeStart " +
            "AND e.eventDate < :rangeEnd")
    List<Event> findEventsForAdmin(List<Long> users, List<EventState> states, List<Integer> categories,
                                   LocalDateTime rangeStart, LocalDateTime rangeEnd, Pageable page);

    @Query("select e from Event e " +
            "JOIN FETCH e.initiator " +
            "JOIN FETCH e.category " +
            "JOIN fetch e.location " +
            "WHERE e.id = :id")
    Optional<Event> findByIdWithAllFields(Long id);

    @Query("select e from Event e " +
            "JOIN FETCH e.initiator " +
            "JOIN FETCH e.category " +
            "JOIN fetch e.location " +
            "WHERE e.id in :ids")
    List<Event> findAllByIdIn(List<Long> ids);

    @Query("select e from Event e " +
            "where e.state = 'PUBLISHED' " +
            "and (:text is null or upper(e.annotation) like upper(concat('%', :text, '%')) or upper(e.description) " +
            "like upper(concat('%', :text, '%')))" +
            "and (:categories is null or e.category.id in :categories) " +
            "and (:paid is null or e.paid = :paid) " +
            "and e.eventDate > :rangeStart " +
            "and e.eventDate < :rangeEnd " +
            "and (:onlyAvailable is null or e.confirmedRequests < e.participantLimit or e.participantLimit = 0)")
    List<Event> findEventsForPublic(String text, List<Integer> categories, Boolean paid, LocalDateTime rangeStart,
                                    LocalDateTime rangeEnd, Boolean onlyAvailable, Pageable page);

}
