package ru.practicum.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.dto.ParticipationRequestDto;
import ru.practicum.service.RequestService;

import java.util.List;

@RestController
@RequestMapping("/users")
@Slf4j
public class RequestController {

    private final RequestService requestService;

    @Autowired
    public RequestController(RequestService requestService) {
        this.requestService = requestService;
    }

    @PostMapping("/{userId}/requests")
    @ResponseStatus(HttpStatus.CREATED)
    public ParticipationRequestDto addRequest(@PathVariable Long userId, @RequestParam Long eventId) {
        log.info("Received POST request to add a participation request from user by id {} to the event by id {}",
                userId, eventId);
        return requestService.addRequest(userId, eventId);
    }

    @GetMapping("/{userId}/requests")
    public List<ParticipationRequestDto> findUserRequests(@PathVariable Long userId) {
        log.info("Received GET request to find all requests for the user by id {}", userId);
        return requestService.findUserRequests(userId);
    }

    @PatchMapping("/{userId}/requests/{requestId}/cancel")
    public ParticipationRequestDto cancelRequest(@PathVariable Long userId, @PathVariable Long requestId) {
        log.info("Received PATCH request to cancel request by the id {} from user by id {}", requestId, userId);
        return requestService.cancelRequest(userId, requestId);
    }
}
