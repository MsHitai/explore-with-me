package ru.practicum.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.dto.EventRequestStatusUpdateRequest;
import ru.practicum.dto.EventRequestStatusUpdateResult;
import ru.practicum.dto.ParticipationRequestDto;
import ru.practicum.exception.DataConflictException;
import ru.practicum.exception.DataNotFoundException;
import ru.practicum.mapper.RequestMapper;
import ru.practicum.model.*;
import ru.practicum.repository.EventRepository;
import ru.practicum.repository.RequestRepository;
import ru.practicum.repository.UserRepository;
import ru.practicum.service.RequestService;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class RequestServiceImpl implements RequestService {

    private final RequestRepository requestRepository;
    private final UserRepository userRepository;
    private final EventRepository eventRepository;

    @Override
    public ParticipationRequestDto addRequest(Long userId, Long eventId) {
        User requester = checkUserId(userId);
        Event event = checkEventId(eventId);
        checkEventAndRequester(requester, event);
        ParticipationRequest request = ParticipationRequest.builder()
                .created(LocalDateTime.now())
                .event(event)
                .requester(requester)
                .status(Status.PENDING)
                .build();
        if (!event.getRequestModeration() || event.getParticipantLimit() == 0) {
            request.setStatus(Status.CONFIRMED);
        }
        return RequestMapper.mapToDto(requestRepository.save(request));
    }

    @Override
    public List<ParticipationRequestDto> findUserRequests(Long userId) {
        checkUserId(userId);
        return requestRepository.findAllByRequesterId(userId).stream()
                .map(RequestMapper::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    public ParticipationRequestDto cancelRequest(Long userId, Long requesterId) {
        checkUserId(userId);
        ParticipationRequest request = checkRequest(requesterId);
        request.setStatus(Status.CANCELED);
        return RequestMapper.mapToDto(requestRepository.save(request));
    }

    @Override
    public EventRequestStatusUpdateResult changeRequestStatus(Long userId, Long eventId,
                                                              EventRequestStatusUpdateRequest update) {
        checkUserId(userId);
        Event event = checkEventId(eventId);
        List<Long> ids = update.getRequestIds();
        List<ParticipationRequest> requests = requestRepository.findAllByIdIn(ids);
        if (!event.getRequestModeration() || event.getParticipantLimit() == 0) {
            return confirmStatus(requests);
        }
        checkLimit(event);
        List<ParticipationRequestDto> confirmedRequests = new ArrayList<>();
        List<ParticipationRequestDto> rejectedRequests = new ArrayList<>();
        for (ParticipationRequest request : requests) {
            checkRequestStatus(request);
            if (event.getConfirmedRequests() + 1 > event.getParticipantLimit()) {
                request.setStatus(Status.CANCELED);
                requestRepository.save(request);
                rejectedRequests.add(RequestMapper.mapToDto(request));
            }
            if (update.getStatus().equals(Status.CONFIRMED)) {
                request.setStatus(Status.CONFIRMED);
                requestRepository.save(request);
                confirmedRequests.add(RequestMapper.mapToDto(request));
            } else {
                request.setStatus(Status.REJECTED);
                requestRepository.save(request);
                rejectedRequests.add(RequestMapper.mapToDto(request));
            }
        }

        return new EventRequestStatusUpdateResult(confirmedRequests, rejectedRequests);
    }

    private EventRequestStatusUpdateResult confirmStatus(List<ParticipationRequest> requests) {
        for (ParticipationRequest request : requests) {
            checkRequestStatus(request);
            request.setStatus(Status.CONFIRMED);
            requestRepository.save(request);
        }
        List<ParticipationRequestDto> dtos = requests.stream()
                .map(RequestMapper::mapToDto)
                .collect(Collectors.toList());
        return new EventRequestStatusUpdateResult(dtos, new ArrayList<>());
    }

    private void checkRequestStatus(ParticipationRequest request) {
        if (!request.getStatus().equals(Status.PENDING)) {
            throw new DataConflictException("The request should have status PENDING for request confirmation");
        }
    }

    private void checkEventAndRequester(User requester, Event event) {
        if (!event.getState().equals(EventState.PUBLISHED)) {
            throw new DataConflictException(String.format("The event by the id=%d is not published", event.getId()));
        }
        checkLimit(event);
        checkIfRequesterIsInitiator(requester, event);
        checkDoubleRequest(requester.getId(), event.getId());
    }

    private static void checkLimit(Event event) {
        //если мы прибавим 1 участника к кол-ву подтвержденных запросов, не будет ли больше лимита на участников
        if (event.getConfirmedRequests() + 1 > event.getParticipantLimit() && event.getParticipantLimit() != 0) {
            throw new DataConflictException(String.format("The event by the id=%d has already reached its " +
                    "participant limit=%d", event.getId(), event.getParticipantLimit()));
        }
    }

    private void checkIfRequesterIsInitiator(User requester, Event event) {
        if (event.getInitiator().equals(requester)) {
            throw new DataConflictException(String.format("The requester by the id=%d is the initiator of the event " +
                    "by the id=%d", requester.getId(), event.getId()));
        }
    }

    private void checkDoubleRequest(Long userId, Long eventId) {
        ParticipationRequest request = requestRepository.findByRequesterIdAndEventId(userId, eventId);
        if (request != null) {
            throw new DataConflictException(String.format("There is already a request from this user by the id=%d to " +
                    "the event by the id=%d", userId, eventId));
        }
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

    private ParticipationRequest checkRequest(Long requestId) {
        return requestRepository.findById(requestId).orElseThrow(() -> new DataNotFoundException(
                String.format("Request with the id=%d was not found in the database", requestId)
        ));
    }
}
