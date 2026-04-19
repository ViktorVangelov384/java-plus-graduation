package ru.practicum.user.service;

import ru.practicum.dto.user.UserShortDto;
import ru.practicum.user.dto.*;

import java.util.List;
import java.util.Map;

public interface UserService {
    void deleteUser(long userId);

    UserShortDto createUser(UserRequestDto newUserRequest);

    List<UserShortDto> getAllUsers(UserAdminParam params);

    Map<Long, UserShortDto> getAllUsersByIds(List<Long> userIds);

    void checkUserExists(Long id);

    List<UserShortDto> getUsersByIds(List<Long> ids);

}