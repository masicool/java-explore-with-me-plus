package ru.practicum.ewm.main.user.repository;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.ewm.main.user.model.User;

import java.util.List;

public interface UserRepository extends JpaRepository<User, Long> {

    List<User> findByIdIn(List<Long> ids, PageRequest page);
}
