package ru.practicum.mapper;

import lombok.experimental.UtilityClass;
import ru.practicum.dto.CategoryDto;
import ru.practicum.dto.CategoryShortDto;
import ru.practicum.model.Category;

@UtilityClass
public class CategoryMapper {

    public CategoryDto mapToDto(Category category) {
        return new CategoryDto(
                category.getId(),
                category.getName()
        );
    }

    public Category mapToCategory(CategoryDto dto) {
        return new Category(
                dto.getId(),
                dto.getName()
        );
    }

    public CategoryShortDto mapToShortDto(Integer id) {
        return new CategoryShortDto(id);
    }
}
