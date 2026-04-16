package ru.practicum.user.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.dto.user.UserShortDto;
import ru.practicum.user.exception.NotFoundException;
import ru.practicum.user.mapper.UserMapper;
import ru.practicum.user.model.User;
import ru.practicum.user.repository.UserRepository;
import ru.practicum.user.service.UserService;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/internal/users")
public class InternalUserController {
    private final UserService userService;
    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @PostMapping
    @ResponseStatus(HttpStatus.OK)
    public Map<Long, UserShortDto> getAllUsersByIds(@RequestBody List<Long> userIds) {
        return userService.getAllUsersByIds(userIds);
    }

    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public UserShortDto getById(@PathVariable Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("User with id=" + id + " not found"));
        return userMapper.toUserShortDto(user);

    }

    @GetMapping("/check/{id}")
    @ResponseStatus(HttpStatus.OK)
    public void checkUserExists(@PathVariable Long id) {
        userService.checkUserExists(id);
    }

    @PostMapping("/by-ids")
    public List<UserShortDto> getUsersByIds(@RequestBody List<Long> ids) {
        return userService.getUsersByIds(ids);
    }
}