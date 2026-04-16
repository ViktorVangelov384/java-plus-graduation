package ru.practicum.user.controller;


import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.dto.user.UserShortDto;
import ru.practicum.user.dto.UserAdminParam;
import ru.practicum.user.dto.UserDto;
import ru.practicum.user.dto.UserRequestDto;
import ru.practicum.user.service.UserService;

import java.util.List;


@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/admin/users")
@Validated
public class UserController {
    private final UserService userService;

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<UserShortDto> getAllUsers(@RequestParam(required = false) List<Long> ids,
                                          @RequestParam(defaultValue = "0") @PositiveOrZero Integer from,
                                          @RequestParam(defaultValue = "10") @Positive Integer size) {
        log.info("GET /admin/users - Fetching users with ids={}, from={}, size={}", ids, from, size);

        UserAdminParam params = new UserAdminParam();
        params.setFrom(from);
        params.setSize(size);
        params.setIds(ids);

        List<UserShortDto> result = userService.getAllUsers(params);
        log.info("GET /admin/users - Returning {} users", result.size());

        return result;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserShortDto createUser(@Valid @RequestBody UserRequestDto newUserRequest) {
        log.info("POST /admin/users - Creating user: {}", newUserRequest);
        UserShortDto created = userService.createUser(newUserRequest);
        log.info("POST /admin/users - User created with id={}", created.getId());
        return created;
    }

    @DeleteMapping("/{userId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteUser(@PathVariable Long userId) {
        log.info("DELETE /admin/users/{} - Deleting user", userId);
        userService.deleteUser(userId);
        log.info("DELETE /admin/users/{} - User deleted", userId);
    }
}