package ru.practicum.service;

import ru.practicum.EndpointHitDto;
import ru.practicum.ViewStatsDto;

import java.time.LocalDateTime;
import java.util.List;

public interface StatsService {
    EndpointHitDto createHit(EndpointHitDto dto);

    List<ViewStatsDto> findStats(boolean unique, LocalDateTime start, LocalDateTime end, List<String> uris);

}
