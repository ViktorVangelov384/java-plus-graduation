package ru.yandex.practicum.user.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import ru.yandex.practicum.user.dto.UserDto;
import ru.yandex.practicum.user.dto.UserRequestDto;
import ru.yandex.practicum.user.dto.UserUpdateDto;

import java.util.List;


public interface UserService {
    UserDto get(Long id);

    Page<UserDto> get(List<Long> ids, Pageable pageable);

    List<UserDto> get(List<Long> ids);

    UserDto create(UserRequestDto user);

    void delete(Long id);

    boolean existsById(Long id);

    boolean checkEmailExist(String email);

    UserDto patch(UserUpdateDto user);
}