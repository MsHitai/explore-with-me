package ru.practicum.service;

import ru.practicum.dto.EventRequestStatusUpdateRequest;
import ru.practicum.dto.EventRequestStatusUpdateResult;
import ru.practicum.dto.ParticipationRequestDto;

import java.util.List;

public interface RequestService {
    ParticipationRequestDto addRequest(Long userId, Long eventId);

    List<ParticipationRequestDto> findUserRequests(Long userId);

    ParticipationRequestDto cancelRequest(Long userId, Long requesterId);

    EventRequestStatusUpdateResult changeRequestStatus(Long userId, Long eventId,
                                                       EventRequestStatusUpdateRequest update);
}
