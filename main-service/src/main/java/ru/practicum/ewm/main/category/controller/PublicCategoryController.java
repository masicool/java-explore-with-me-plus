package ru.practicum.ewm.main.category.controller;

import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.main.category.dto.CategoryDto;
import ru.practicum.ewm.main.category.service.CategoryService;

import java.util.List;

@RestController
@RequestMapping("/categories")
@RequiredArgsConstructor
public class PublicCategoryController {
    public final CategoryService categoryService;

    @GetMapping("/{catId}")
    @ResponseStatus(HttpStatus.OK)
    public CategoryDto findCategoryById(@PathVariable long catId) {
        return categoryService.findCategoryById(catId);
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<CategoryDto> findAllCategories(@RequestParam(defaultValue = "0") @PositiveOrZero int from,
                                               @RequestParam(defaultValue = "10") @Positive int size) {
        return categoryService.findAllCategories(from, size);
    }
}
