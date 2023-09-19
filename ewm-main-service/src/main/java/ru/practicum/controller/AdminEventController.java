package ru.practicum.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.dto.EventFullDto;
import ru.practicum.dto.UpdateEventAdminRequest;
import ru.practicum.model.EventState;
import ru.practicum.service.EventService;

import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/admin/events")
@Validated
@Slf4j
public class AdminEventController {

    private final EventService eventService;

    @Autowired
    public AdminEventController(EventService eventService) {
        this.eventService = eventService;
    }

    @GetMapping()
    @ResponseStatus(HttpStatus.OK)
    public List<EventFullDto> findEventsForAdmin(@PositiveOrZero @RequestParam(defaultValue = "0") Integer from,
                                                 @Positive @RequestParam(defaultValue = "10") Integer size,
                                                 @RequestParam(required = false) List<Long> users,
                                                 @RequestParam(required = false) List<EventState> states,
                                                 @RequestParam(required = false) List<Integer> categories,
                                                 @RequestParam(required = false)
                                                 @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
                                                 LocalDateTime rangeStart,
                                                 @RequestParam(required = false)
                                                 @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
                                                 LocalDateTime rangeEnd) {
        log.info("Received GET request from Admin to find events from {} to {} with users {}, states {}, " +
                        "categories {} rangeStart {}, rangeEnd {}", from, size, users, states,
                categories, rangeStart, rangeEnd);
        Pageable page = PageRequest.of(from / size, size);
        return eventService.findEventsForAdmin(users, states, categories, rangeStart, rangeEnd, page);
    }

    @PatchMapping("/{eventId}")
    @ResponseStatus(HttpStatus.OK)
    public EventFullDto patchEventStatus(@PathVariable Long eventId, @RequestBody UpdateEventAdminRequest updates) {
        log.info("Received PATCH request to change event status with updates {} of the event by id {} by admin",
                updates.toString(), eventId);
        return eventService.patchEventAdmin(eventId, updates);
    }

}
