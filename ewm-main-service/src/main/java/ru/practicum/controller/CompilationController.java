package ru.practicum.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.dto.CompilationDto;
import ru.practicum.service.CompilationService;

import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import java.util.List;

@RestController
@RequestMapping("/compilations")
@Validated
@Slf4j
public class CompilationController {

    private final CompilationService compilationService;

    @Autowired
    public CompilationController(CompilationService compilationService) {
        this.compilationService = compilationService;
    }

    @GetMapping()
    public List<CompilationDto> findCompilations(@RequestParam(required = false) Boolean pinned,
                                                 @PositiveOrZero @RequestParam(defaultValue = "0") Integer from,
                                                 @Positive @RequestParam(defaultValue = "10") Integer size) {
        log.info("Received GET request to find compilations with the pinned {} from {} to {}", pinned, from, size);
        Pageable page = PageRequest.of(from / size, size);
        return compilationService.findCompilations(pinned, page);
    }

    @GetMapping("/{compId}")
    public CompilationDto findById(@PathVariable Long compId) {
        log.info("Received GET request to find compilation by id {}", compId);
        return compilationService.findById(compId);
    }
}
