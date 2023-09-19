package ru.practicum.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import ru.practicum.dto.CompilationDto;
import ru.practicum.dto.UpdateCompilationDto;
import ru.practicum.exception.DataNotFoundException;
import ru.practicum.exception.ValidationException;
import ru.practicum.mapper.CompilationMapper;
import ru.practicum.model.Compilation;
import ru.practicum.model.Event;
import ru.practicum.repository.CompilationRepository;
import ru.practicum.repository.EventRepository;
import ru.practicum.service.CompilationService;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CompilationServiceImpl implements CompilationService {

    private final CompilationRepository compilationRepository;
    private final EventRepository eventRepository;

    @Override
    public List<CompilationDto> findCompilations(Boolean pinned, Pageable page) {
        return compilationRepository.findAllByPinned(pinned, page).stream()
                .map(CompilationMapper::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    public CompilationDto findById(Long compId) {
        Compilation compilation = checkCompId(compId);
        return CompilationMapper.mapToDto(compilation);
    }

    @Override
    public CompilationDto addCompilation(UpdateCompilationDto dto) {
        List<Event> events = eventRepository.findAllByIdIn(dto.getEvents());
        Compilation compilation = new Compilation();
        compilation.setEvents(events);
        if (dto.getPinned() == null) {
            compilation.setPinned(false);
        } else {
            compilation.setPinned(dto.getPinned());
        }
        compilation.setTitle(dto.getTitle());
        compilation = compilationRepository.save(compilation);
        return CompilationMapper.mapToDto(compilation);
    }

    @Override
    public void deleteCompilation(Long compId) {
        try {
            compilationRepository.deleteById(compId);
        } catch (EmptyResultDataAccessException e) {
            throw new DataNotFoundException(
                    String.format("Compilation with the id=%d was not found in the database", compId));
        }
    }

    @Override
    public CompilationDto patchCompilation(Long compId, UpdateCompilationDto update) {
        Compilation compilation = checkCompId(compId);
        if (update.getPinned() != null) {
            compilation.setPinned(update.getPinned());
        }
        if (update.getTitle() != null) {
            if (update.getTitle().length() >= 1 && update.getTitle().length() <= 50) {
                compilation.setTitle(update.getTitle());
            } else {
                throw new ValidationException("The title length should be between 1 and 50 characters");
            }
        }
        List<Event> events = eventRepository.findAllByIdIn(update.getEvents());
        compilation.setEvents(events);
        return CompilationMapper.mapToDto(compilationRepository.save(compilation));
    }

    private Compilation checkCompId(Long compId) {
        return compilationRepository.findById(compId).orElseThrow(() -> new DataNotFoundException(
                String.format("Compilation with the id=%d is not found in the database", compId)));
    }
}
