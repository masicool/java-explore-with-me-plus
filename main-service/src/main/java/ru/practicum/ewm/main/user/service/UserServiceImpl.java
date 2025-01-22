package ru.practicum.ewm.main.user.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.main.exception.type.NotFoundException;
import ru.practicum.ewm.main.user.dto.NewUserRequest;
import ru.practicum.ewm.main.user.dto.UserDto;
import ru.practicum.ewm.main.user.model.User;
import ru.practicum.ewm.main.user.repository.UserRepository;

import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserServiceImpl implements UserService {
    final UserRepository userRepository;
    final ModelMapper modelMapper;

    @Override
    public UserDto addUser(NewUserRequest newUserRequest) {
        return modelMapper.map(userRepository.save(modelMapper.map(newUserRequest, User.class)), UserDto.class);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserDto> findUsersByRequest(List<Long> userIds, int from, int size) {
        PageRequest page = PageRequest.of(from > 0 ? from / size : 0, size);
        if (userIds == null) {
            return userRepository.findAll(page).stream()
                    .map(o -> modelMapper.map(o, UserDto.class))
                    .toList();
        }
        return userRepository.findByIdIn(userIds, page).stream()
                .map(o -> modelMapper.map(o, UserDto.class))
                .toList();
    }

    @Override
    public void deleteUserById(long userId) {
        userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User with id=" + userId + " was not found"));
        userRepository.deleteById(userId);
    }
}
