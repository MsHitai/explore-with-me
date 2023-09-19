package ru.practicum.service;

import ru.practicum.dto.CategoryDto;

import java.util.List;

public interface CategoryService {
    CategoryDto addCategory(CategoryDto dto);

    void deleteCategory(Integer catId);

    CategoryDto patchCategory(Integer catId, CategoryDto dto);

    List<CategoryDto> findAllCategories(Integer from, Integer size);

    CategoryDto findCategoryById(Integer catId);

}
