package ru.practicum.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.dto.CategoryDto;
import ru.practicum.exception.DataNotFoundException;
import ru.practicum.exception.ValidationException;
import ru.practicum.mapper.CategoryMapper;
import ru.practicum.model.Category;
import ru.practicum.repository.CategoryRepository;
import ru.practicum.service.CategoryService;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;

    @Autowired
    public CategoryServiceImpl(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }


    @Override
    public CategoryDto addCategory(CategoryDto dto) {
        Category category = CategoryMapper.mapToCategory(dto);
        return CategoryMapper.mapToDto(categoryRepository.save(category));
    }

    @Override
    public void deleteCategory(Integer catId) {
        try {
            categoryRepository.deleteById(catId);
        } catch (EmptyResultDataAccessException e) {
            throw new DataNotFoundException(
                    String.format("Category with the id=%d was not found in the database", catId));
        }
    }

    @Override
    public CategoryDto patchCategory(Integer catId, CategoryDto dto) {
        Category category = checkCatId(catId);
        if (dto.getName().length() >= 1 && dto.getName().length() <= 50) {
            category.setName(dto.getName());
        } else {
            throw new ValidationException("The name of the category should be between 1 to 50 characters");
        }

        return CategoryMapper.mapToDto(categoryRepository.save(category));
    }

    @Override
    public List<CategoryDto> findAllCategories(Integer from, Integer size) {
        Pageable page = PageRequest.of(from / size, size);
        return categoryRepository.findAll(page).stream()
                .map(CategoryMapper::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    public CategoryDto findCategoryById(Integer catId) {
        Category category = checkCatId(catId);
        return CategoryMapper.mapToDto(category);
    }

    private Category checkCatId(Integer catId) {
        return categoryRepository.findById(catId).orElseThrow(() -> new DataNotFoundException(
                String.format("Category with the id=%d was not found in the database", catId)));
    }
}
