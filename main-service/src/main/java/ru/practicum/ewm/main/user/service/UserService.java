package ru.practicum.ewm.main.user.service;

import ru.practicum.ewm.main.user.dto.NewUserRequest;
import ru.practicum.ewm.main.user.dto.UserDto;

import java.util.List;

public interface UserService {
    UserDto addUser(NewUserRequest newUserRequest);

    List<UserDto> findUsersByRequest(List<Long> userIds, int from, int size);

    void deleteUserById(long catId);
}
