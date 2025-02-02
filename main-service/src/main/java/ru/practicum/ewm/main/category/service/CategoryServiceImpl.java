package ru.practicum.ewm.main.category.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.main.category.dto.CategoryDto;
import ru.practicum.ewm.main.category.dto.NewCategoryDto;
import ru.practicum.ewm.main.category.model.Category;
import ru.practicum.ewm.main.category.repository.CategoryRepository;
import ru.practicum.ewm.main.exception.type.NotFoundException;

import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CategoryServiceImpl implements CategoryService {
    CategoryRepository categoryRepository;
    ModelMapper modelMapper;

    @Override
    public CategoryDto addCategory(NewCategoryDto newCategoryDto) {
        return modelMapper.map(categoryRepository.save(modelMapper.map(newCategoryDto, Category.class)), CategoryDto.class);
    }

    @Override
    public CategoryDto updateCategoryById(long catId, NewCategoryDto newCategoryDto) {
        findCategory(catId);
        Category categoryToUpdate = modelMapper.map(newCategoryDto, Category.class);
        categoryToUpdate.setId(catId);
        return modelMapper.map(categoryRepository.save(categoryToUpdate), CategoryDto.class);
    }

    @Override
    public void deleteCategoryById(long catId) {
        findCategory(catId);
        categoryRepository.deleteById(catId);
    }

    @Override
    @Transactional(readOnly = true)
    public CategoryDto findCategoryById(long catId) {
        return modelMapper.map(findCategory(catId), CategoryDto.class);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoryDto> findAllCategories(int from, int size) {
        PageRequest page = PageRequest.of(from, size);
        return categoryRepository.findAll(page).stream()
                .map(o -> modelMapper.map(o, CategoryDto.class))
                .toList();
    }

    private Category findCategory(long catId) {
        return categoryRepository.findById(catId)
                .orElseThrow(() -> new NotFoundException("Category with id=" + catId + " was not found"));
    }
}
