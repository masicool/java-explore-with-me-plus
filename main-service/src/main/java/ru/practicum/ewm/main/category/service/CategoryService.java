package ru.practicum.ewm.main.category.service;

import ru.practicum.ewm.main.category.dto.CategoryDto;
import ru.practicum.ewm.main.category.dto.NewCategoryDto;

import java.util.List;

public interface CategoryService {
    CategoryDto addCategory(NewCategoryDto newCategoryDto);

    CategoryDto updateCategoryById(long catId, NewCategoryDto newCategoryDto);

    void deleteCategoryById(long catId);

    CategoryDto findCategoryById(long catId);

    List<CategoryDto> findAllCategories(int from, int size);
}
