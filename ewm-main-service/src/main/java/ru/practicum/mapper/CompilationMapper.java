package ru.practicum.mapper;

import lombok.experimental.UtilityClass;
import ru.practicum.dto.CompilationDto;
import ru.practicum.dto.EventShortDto;
import ru.practicum.model.Compilation;

import java.util.List;
import java.util.stream.Collectors;

@UtilityClass
public class CompilationMapper {

    public CompilationDto mapToDto(Compilation compilation) {
        CompilationDto dto = CompilationDto.builder()
                .id(compilation.getId())
                .pinned(compilation.getPinned())
                .title(compilation.getTitle())
                .build();

        List<EventShortDto> events = compilation.getEvents().stream()
                .map(EventMapper::mapToShortDto)
                .collect(Collectors.toList());

        dto.setEvents(events);

        return dto;
    }
}
