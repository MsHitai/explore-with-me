package ru.practicum.service;

import org.springframework.data.domain.Pageable;
import ru.practicum.dto.EventFullDto;
import ru.practicum.dto.EventShortDto;
import ru.practicum.dto.ParticipationRequestDto;
import ru.practicum.dto.UpdateEventAdminRequest;
import ru.practicum.model.EventState;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public interface EventService {
    EventFullDto addEvent(Long userId, EventFullDto dto);

    List<EventShortDto> findAllEvents(Long userId, Integer from, Integer size);

    EventFullDto findById(Long userId, Long eventId);

    EventFullDto patchEvent(Long userId, Long eventId, Map<String, Object> updates);

    List<ParticipationRequestDto> findParticipationRequests(Long userId, Long eventId);

    List<EventFullDto> findEventsForAdmin(List<Long> users, List<EventState> states, List<Integer> categories,
                                          LocalDateTime rangeStart, LocalDateTime rangeEnd, Pageable page);

    EventFullDto patchEventAdmin(Long eventId, UpdateEventAdminRequest updates);

    List<EventShortDto> findAllEventsForPublic(String text, List<Integer> categories, Boolean paid,
                                               LocalDateTime rangeStart, LocalDateTime rangeEnd, Boolean onlyAvailable,
                                               String sort, Integer from, Integer size, HttpServletRequest request);

    EventFullDto findByIdPublic(Long eventId, HttpServletRequest request);
}
