package ru.practicum.ewm.main.compilation.repository;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.ewm.main.compilation.model.Compilation;

import java.util.List;

public interface CompilationRepository extends JpaRepository<Compilation, Long> {

    @Query("select c " +
            "from Compilation c " +
            "where ?1 is null or c.pinned = ?1")
    List<Compilation> findAllByPinned(Boolean pinned, PageRequest pageRequest);
}
