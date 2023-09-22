package ru.practicum.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.EndpointHitDto;
import ru.practicum.StatsClient;
import ru.practicum.ViewStatsDto;
import ru.practicum.dto.*;
import ru.practicum.exception.DataConflictException;
import ru.practicum.exception.DataNotFoundException;
import ru.practicum.exception.ValidationException;
import ru.practicum.mapper.EventMapper;
import ru.practicum.mapper.LocationMapper;
import ru.practicum.mapper.RequestMapper;
import ru.practicum.model.*;
import ru.practicum.repository.*;
import ru.practicum.service.EventService;

import javax.servlet.http.HttpServletRequest;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional
public class EventServiceImpl implements EventService {

    public static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final LocationRepository locationRepository;
    private final CategoryRepository categoryRepository;
    private final RequestRepository requestRepository;
    private final StatsClient statsClient;


    @Autowired
    public EventServiceImpl(EventRepository eventRepository, UserRepository userRepository,
                            LocationRepository locationRepository, CategoryRepository categoryRepository,
                            RequestRepository requestRepository, StatsClient statsClient) {
        this.eventRepository = eventRepository;
        this.userRepository = userRepository;
        this.locationRepository = locationRepository;
        this.categoryRepository = categoryRepository;
        this.requestRepository = requestRepository;
        this.statsClient = statsClient;
    }

    @Override
    public EventFullDto addEvent(Long userId, EventFullDto dto) {
        User initiator = checkUserId(userId);
        Location location = LocationMapper.mapToLocation(dto.getLocation());
        Category category = checkCatId(dto.getCategory().getId());
        dto.setCreatedOn(LocalDateTime.now());

        Event event = EventMapper.mapToEvent(dto);
        checkTime(event);
        if (dto.getPaid() == null) {
            event.setPaid(false);
        }
        if (dto.getRequestModeration() == null) {
            event.setRequestModeration(true);
        }
        event.setState(EventState.PENDING);
        event.setInitiator(initiator);
        event.setLocation(location);
        event.setCategory(category);
        locationRepository.save(location);

        return EventMapper.mapToFullDto(eventRepository.save(event));
    }

    @Override
    public List<EventShortDto> findAllEvents(Long userId, Integer from, Integer size) {
        checkUserId(userId);
        Pageable page = PageRequest.of(from / size, size);
        return eventRepository.findAllByInitiatorId(userId, page).stream()
                .map(EventMapper::mapToShortDto)
                .collect(Collectors.toList());
    }

    @Override
    public EventFullDto findById(Long userId, Long eventId) {
        checkUserId(userId);
        checkEventId(eventId);
        Event event = eventRepository.findByIdAndInitiatorId(eventId, userId);
        setViews(event);
        return EventMapper.mapToFullDto(event);
    }

    @Override
    public EventFullDto patchEvent(Long userId, Long eventId, Map<String, Object> updates) {
        checkUserId(userId);
        Event event = checkEventId(eventId);

        if (event.getState().equals(EventState.PUBLISHED)) {
            throw new DataConflictException("Only events with the state PENDING or CANCELED can be patched");
        }

        Event updatedEvent = updateEvent(updates, event);

        checkTime(updatedEvent);

        return EventMapper.mapToFullDto(eventRepository.save(updatedEvent));
    }

    @Override
    public List<ParticipationRequestDto> findParticipationRequests(Long userId, Long eventId) {
        checkUserId(userId);
        checkEventId(eventId);
        List<ParticipationRequest> requests = requestRepository.findAllByEventId(eventId);
        return requests.stream().map(RequestMapper::mapToDto).collect(Collectors.toList());
    }

    @Override
    public List<EventFullDto> findEventsForAdmin(List<Long> users, List<EventState> states, List<Integer> categories,
                                                 LocalDateTime rangeStart, LocalDateTime rangeEnd, Pageable page) {
        if (rangeStart == null) {
            rangeStart = LocalDateTime.now();
        }
        if (rangeEnd == null) {
            rangeEnd = LocalDateTime.now().plusYears(100);
        }
        if (rangeStart.isAfter(rangeEnd)) {
            throw new ValidationException("The start of the event cannot be after the end of the event");
        }
        List<Event> events = eventRepository.findEventsForAdmin(users, states, categories, rangeStart, rangeEnd, page);
        events.forEach(this::setViews);
        return events.stream()
                .map(EventMapper::mapToFullDto)
                .collect(Collectors.toList());
    }

    @Override
    public EventFullDto patchEventAdmin(Long eventId, UpdateEventAdminRequest updates) {
        Event event = checkEventId(eventId);
        Event updatedEvent = updateEvent(updates, event);
        return EventMapper.mapToFullDto(updatedEvent);
    }

    @Override
    public List<EventShortDto> findAllEventsForPublic(SearchEventParams params, Pageable page,
                                                      HttpServletRequest request) {
        LocalDateTime rangeStart = params.getRangeStart();
        LocalDateTime rangeEnd = params.getRangeEnd();
        String sort = params.getSort();
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
        if (sort != null && sort.equals("EVENT_DATE")) {
            events = events.stream()
                    .sorted(Comparator.comparing(Event::getEventDate))
                    .collect(Collectors.toList());
        }
        if (sort != null && sort.equals("VIEWS")) {
            events = events.stream()
                    .sorted(Comparator.comparing(Event::getViews))
                    .collect(Collectors.toList());
        }

        createHit(request.getRequestURI(), request.getRemoteAddr());

        events.forEach(this::setViews);

        return events.stream()
                .map(EventMapper::mapToShortDto)
                .collect(Collectors.toList());
    }

    @Override
    public EventFullDto findByIdPublic(Long eventId, HttpServletRequest request) {
        Event event = checkEventId(eventId);
        if (!event.getState().equals(EventState.PUBLISHED)) {
            throw new DataNotFoundException(String.format("The event by id = %d has not been published yet", eventId));
        }
        createHit(request.getRequestURI(), request.getRemoteAddr());

        setViews(event);
        return EventMapper.mapToFullDto(event);
    }

    private void setViews(Event event) {
        String start = event.getCreatedOn().format(FORMATTER);
        String end = LocalDateTime.now().format(FORMATTER);
        List<ViewStatsDto> stats = statsClient.findStats(start, end, true, List.of("/events/" + event.getId()));
        if (stats.size() == 0) {
            event.setViews(0);
        } else {
            event.setViews(stats.get(0).getHits());
        }
    }

    private Event updateEvent(Map<String, Object> updates, Event event) {
        for (String s : updates.keySet()) {
            switch (s) {
                case "annotation":
                    String annotation = (String) updates.get(s);
                    if (annotation.length() < 20 || annotation.length() > 2000) {
                        throw new ValidationException("The annotation length must be between 20 to 2000 letters");
                    }
                    event.setAnnotation(annotation);
                    break;
                case "category":
                    Integer id = (Integer) updates.get(s);
                    event.setCategory(checkCatId(id));
                    break;
                case "description":
                    String description = (String) updates.get(s);
                    if (description.length() < 20 || description.length() > 7000) {
                        throw new ValidationException("The description length must be between 20 to 7000 letters");
                    }
                    event.setDescription(description);
                    break;
                case "eventDate":
                    String date = (String) updates.get(s);
                    LocalDateTime dateTime = LocalDateTime.parse(date, FORMATTER);
                    event.setEventDate(dateTime);
                    break;
                case "location":
                    Long locId = (Long) updates.get(s);
                    event.setLocation(checkLocId(locId));
                    break;
                case "paid":
                    event.setPaid((Boolean) updates.get(s));
                    break;
                case "participantLimit":
                    event.setParticipantLimit((int) updates.get(s));
                    break;
                case "requestModeration":
                    event.setRequestModeration((boolean) updates.get(s));
                    break;
                case "stateAction":
                    StateAction action = StateAction.valueOf((String) updates.get(s));
                    checkAction(action, event);
                    break;
                case "title":
                    String title = (String) updates.get(s);
                    if (title.length() < 3 || title.length() > 120) {
                        throw new ValidationException("The length of the title must be between 3 to 120 letters");
                    }
                    event.setTitle(title);
                    break;
            }
        }
        return event;
    }

    //два разных метода обновления для админа и пользователей из-за разных типов данных дат, списка StateAction
    private Event updateEvent(UpdateEventAdminRequest updates, Event event) {
        if (updates.getAnnotation() != null) {
            if (updates.getAnnotation().length() < 20 || updates.getAnnotation().length() > 2000) {
                throw new ValidationException("The annotation length must be between 20 to 2000 letters");
            }
            event.setAnnotation(updates.getAnnotation());
        }
        if (updates.getCategory() != null) {
            Category category = checkCatId(updates.getCategory());
            event.setCategory(category);
        }
        if (updates.getDescription() != null) {
            if (updates.getDescription().length() < 20 || updates.getDescription().length() > 7000) {
                throw new ValidationException("The description length must be between 20 to 7000 letters");
            }
            event.setDescription(updates.getDescription());
        }
        if (updates.getEventDate() != null) {
            String date = updates.getEventDate();
            LocalDateTime dateTime = LocalDateTime.parse(date, FORMATTER);
            if (dateTime.isBefore(LocalDateTime.now().plusHours(1))) {
                throw new ValidationException("The difference between the creation time and the event date must be " +
                        "over one hour at least");
            }
            event.setEventDate(dateTime);
        }
        if (updates.getLocation() != null) {
            LocationDto dto = updates.getLocation();
            Location location = LocationMapper.mapToLocation(dto);
            event.setLocation(locationRepository.save(location));
        }
        if (updates.getPaid() != null) {
            event.setPaid(updates.getPaid());
        }
        if (updates.getParticipantLimit() != null) {
            event.setParticipantLimit(updates.getParticipantLimit());
        }
        if (updates.getRequestModeration() != null) {
            event.setRequestModeration(updates.getRequestModeration());
        }
        if (updates.getStateAction() != null) {
            checkAction(updates.getStateAction(), event);
        }
        if (updates.getTitle() != null) {
            if (updates.getTitle().length() < 3 || updates.getTitle().length() > 120) {
                throw new ValidationException("The length of the title must be between 3 to 120 letters");
            }
            event.setTitle(updates.getTitle());
        }
        return eventRepository.save(event);
    }

    private void createHit(String uri, String ip) {
        EndpointHitDto hit = new EndpointHitDto();
        hit.setApp("ewm-main-service");
        hit.setIp(ip);
        hit.setUri(uri);
        hit.setTimestamp(LocalDateTime.now());
        statsClient.createHit(hit);
    }

    private void checkTime(Event event) {
        if (Duration.between(event.getCreatedOn(), event.getEventDate()).compareTo(Duration.ofHours(2)) < 0) {
            throw new ValidationException("The difference between the creation time and the event date must be " +
                    "over two hours at least");
        }
    }

    private void checkAction(StateAction action, Event event) {
        switch (action) {
            case SEND_TO_REVIEW:
                event.setState(EventState.PENDING);
                break;
            case CANCEL_REVIEW:
                event.setState(EventState.CANCELED);
                break;
            case REJECT_EVENT:
                if (event.getState().equals(EventState.PUBLISHED)) {
                    throw new DataConflictException("The event is already published");
                }
                event.setState(EventState.CANCELED);
                break;
            case PUBLISH_EVENT:
                if (!event.getState().equals(EventState.PENDING)) {
                    throw new DataConflictException("The event status should be PENDING to confirm publishing");
                }
                event.setState(EventState.PUBLISHED);
                event.setPublishedOn(LocalDateTime.now());
                break;
        }
    }

    private Category checkCatId(Integer catId) {
        return categoryRepository.findById(catId).orElseThrow(() -> new DataNotFoundException(
                String.format("Category with the id=%d was not found in the database", catId)));
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

    private Location checkLocId(Long locId) {
        return locationRepository.findById(locId).orElseThrow(() -> new DataNotFoundException(
                String.format("Location with the id=%d was not found in the database", locId)
        ));
    }
}
