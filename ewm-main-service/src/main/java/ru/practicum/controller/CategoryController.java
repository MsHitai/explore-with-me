package ru.practicum.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ru.practicum.dto.CategoryDto;
import ru.practicum.service.CategoryService;

import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import java.util.List;

@RestController
@RequestMapping("/categories")
@Slf4j
public class CategoryController {

    private final CategoryService categoryService;


    @Autowired
    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @GetMapping()
    public List<CategoryDto> findAllCategories(@PositiveOrZero @RequestParam(defaultValue = "0") Integer from,
                                               @Positive @RequestParam(defaultValue = "10") Integer size) {
        log.info("Received GET request for all categories from {} to {}", from, size);
        return categoryService.findAllCategories(from, size);
    }

    @GetMapping("/{catId}")
    public CategoryDto findCategoryById(@PathVariable Integer catId) {
        log.info("Received GET request to find a category by id {}", catId);
        return categoryService.findCategoryById(catId);
    }
}
