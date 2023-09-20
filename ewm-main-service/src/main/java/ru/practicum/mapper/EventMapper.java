package ru.practicum.mapper;

import lombok.experimental.UtilityClass;
import ru.practicum.dto.*;
import ru.practicum.model.Event;

@UtilityClass
public class EventMapper {

    public Event mapToEvent(EventFullDto dto) {
        return Event.builder()
                .id(dto.getId())
                .annotation(dto.getAnnotation())
                .confirmedRequests(dto.getConfirmedRequests())
                .description(dto.getDescription())
                .eventDate(dto.getEventDate())
                .paid(dto.getPaid())
                .participantLimit(dto.getParticipantLimit())
                .requestModeration(dto.getRequestModeration())
                .title(dto.getTitle())
                .state(dto.getState())
                .createdOn(dto.getCreatedOn())
                .publishedOn(dto.getPublishedOn())
                .views(dto.getViews())
                .build();
    }

    public EventFullDto mapToFullDto(Event event) {
        EventFullDto dto = EventFullDto.builder()
                .id(event.getId())
                .annotation(event.getAnnotation())
                .confirmedRequests(event.getConfirmedRequests())
                .description(event.getDescription())
                .eventDate(event.getEventDate())
                .paid(event.getPaid())
                .participantLimit(event.getParticipantLimit())
                .requestModeration(event.getRequestModeration())
                .title(event.getTitle())
                .state(event.getState())
                .createdOn(event.getCreatedOn())
                .publishedOn(event.getPublishedOn())
                .views(event.getViews())
                .build();
        UserShortDto userShort = UserMapper.mapToShortDto(event.getInitiator());
        CategoryShortDto categoryDto = CategoryMapper.mapToShortDto(event.getCategory().getId());
        LocationDto locationDto = LocationMapper.mapToDto(event.getLocation());

        dto.setInitiator(userShort);
        dto.setCategory(categoryDto);
        dto.setLocation(locationDto);

        return dto;
    }

    public EventShortDto mapToShortDto(Event event) {
        EventShortDto dto = EventShortDto.builder()
                .id(event.getId())
                .annotation(event.getAnnotation())
                .confirmedRequests(event.getConfirmedRequests())
                .eventDate(event.getEventDate())
                .paid(event.getPaid())
                .title(event.getTitle())
                .views(event.getViews())
                .build();
        UserShortDto userShort = UserMapper.mapToShortDto(event.getInitiator());
        CategoryDto categoryDto = CategoryMapper.mapToDto(event.getCategory());

        dto.setInitiator(userShort);
        dto.setCategory(categoryDto);

        return dto;
    }

    public EventShortRatingDto mapToShortRatingDto(Event event, double rating) {
        EventShortRatingDto dto = EventShortRatingDto.builder()
                .id(event.getId())
                .annotation(event.getAnnotation())
                .confirmedRequests(event.getConfirmedRequests())
                .eventDate(event.getEventDate())
                .paid(event.getPaid())
                .title(event.getTitle())
                .views(event.getViews())
                .rating(rating)
                .build();
        UserShortDto userShort = UserMapper.mapToShortDto(event.getInitiator());
        CategoryDto categoryDto = CategoryMapper.mapToDto(event.getCategory());

        dto.setInitiator(userShort);
        dto.setCategory(categoryDto);

        return dto;
    }
}
