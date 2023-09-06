package ru.practicum.mapper;

import lombok.experimental.UtilityClass;
import ru.practicum.EndpointHitDto;
import ru.practicum.ViewStatsDto;
import ru.practicum.model.EndpointHit;

@UtilityClass
public class HitMapper {

    public EndpointHit mapToHit(EndpointHitDto dto) {
        return EndpointHit.builder()
                .id(dto.getId())
                .app(dto.getApp())
                .uri(dto.getUri())
                .ip(dto.getIp())
                .timestamp(dto.getTimestamp())
                .build();
    }

    public EndpointHitDto mapToHitDto(EndpointHit hit) {
        return new EndpointHitDto(
                hit.getId(),
                hit.getApp(),
                hit.getUri(),
                hit.getIp(),
                hit.getTimestamp()
        );
    }

    public ViewStatsDto mapToViewDto(EndpointHit hit) {
        return ViewStatsDto.builder()
                .app(hit.getApp())
                .uri(hit.getUri())
                .build();
    }
}
