package ru.practicum.ewm.main.category.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.ewm.main.category.model.Category;

public interface CategoryRepository extends JpaRepository<Category, Long> {
}
