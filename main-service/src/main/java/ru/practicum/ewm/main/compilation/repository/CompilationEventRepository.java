package ru.practicum.ewm.main.compilation.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.ewm.main.compilation.model.Compilation;
import ru.practicum.ewm.main.compilation.model.CompilationEvent;

import java.util.List;

public interface CompilationEventRepository extends JpaRepository<Compilation, Long> {
    @Query("select ce " +
            "from CompilationEvent ce " +
            "where ce.compilation.id in ?1")
    List<CompilationEvent> findByCompilations(List<Long> compilations);
}
