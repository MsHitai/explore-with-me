package ru.practicum.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.dto.*;
import ru.practicum.service.EventService;
import ru.practicum.service.RequestService;

import javax.validation.Valid;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/users")
@Validated
@Slf4j
public class EventPrivateController {

    private final EventService eventService;
    private final RequestService requestService;

    @Autowired
    public EventPrivateController(EventService eventService, RequestService requestService) {
        this.eventService = eventService;
        this.requestService = requestService;
    }

    @PostMapping("/{userId}/events")
    @ResponseStatus(HttpStatus.CREATED)
    public EventFullDto addEvent(@PathVariable Long userId,
                                 @Valid @RequestBody EventFullDto dto) {
        log.info("Received POST request to add an event {} from user by id {}", dto.toString(), userId);
        return eventService.addEvent(userId, dto);
    }

    @GetMapping("/{userId}/events")
    public List<EventShortDto> findAllEvents(@PathVariable Long userId,
                                             @PositiveOrZero @RequestParam(defaultValue = "0") Integer from,
                                             @Positive @RequestParam(defaultValue = "10") Integer size) {
        log.info("Received GET request for all categories from {} to {}", from, size);
        return eventService.findAllEvents(userId, from, size);
    }

    @GetMapping("/{userId}/events/{eventId}")
    public EventFullDto findById(@PathVariable Long userId, @PathVariable Long eventId) {
        log.info("Received GET request to find an event by id {} from user by id {}", eventId, userId);
        return eventService.findById(userId, eventId);
    }

    @PatchMapping("/{userId}/events/{eventId}")
    public EventFullDto patchEvent(@PathVariable Long userId, @PathVariable Long eventId,
                                   @RequestBody Map<String, Object> updates) {
        log.info("Received PATCH request to update event by id {} from user by id {} with the " +
                "following changes {}", eventId, userId, updates.toString());
        return eventService.patchEvent(userId, eventId, updates);
    }

    @GetMapping("/{userId}/events/{eventId}/requests")
    public List<ParticipationRequestDto> findParticipationRequests(@PathVariable Long userId,
                                                                   @PathVariable Long eventId) {
        log.info("Received GET request to find participant requests for the event by id {} by current user by id {}",
                eventId, userId);
        return eventService.findParticipationRequests(userId, eventId);
    }

    @PatchMapping("/{userId}/events/{eventId}/requests")
    public EventRequestStatusUpdateResult changeRequestStatus(@PathVariable Long userId, @PathVariable Long eventId,
                                                              @RequestBody EventRequestStatusUpdateRequest update) {
        log.info("Received PATCH request to change request status from user by id {} to the event by id {} " +
                "with the following updates {}", userId, eventId, update.toString());
        return requestService.changeRequestStatus(userId, eventId, update);
    }
}
