package ru.practicum.user.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import ru.practicum.dto.user.UserShortDto;
import ru.practicum.user.dto.*;
import ru.practicum.user.exception.DataAlreadyInUseException;
import ru.practicum.user.exception.ValidationException;
import ru.practicum.user.mapper.UserMapper;
import ru.practicum.user.model.User;
import ru.practicum.user.repository.UserRepository;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Override
    public List<UserShortDto> getAllUsers(UserAdminParam param) {
        int from = param.getFrom();
        int size = param.getSize();

        int pageNumber = from / size;
        Pageable pageable = PageRequest.of(pageNumber, size);

        if (CollectionUtils.isEmpty(param.getIds())) {
            Page<User> userPage = userRepository.findAll(pageable);
            List<User> users = userPage.getContent();
            return users.stream()
                    .map(userMapper::toUserShortDto)
                    .collect(Collectors.toList());
        }

        List<User> users = userRepository.findAllByIds(param.getIds(), pageable);
        return users.stream()
                .map(userMapper::toUserShortDto)
                .collect(Collectors.toList());
    }

    @Override
    public Map<Long, UserShortDto> getAllUsersByIds(List<Long> userIds) {
        List<User> users = userRepository.findByIdIn(userIds);
        return users.stream()
                .collect(Collectors.toMap(
                        User::getId,
                        userMapper::toUserShortDto
                ));
    }

    @Override
    public void checkUserExists(Long id) {
        if (!userRepository.existsById(id)) {
            throw new RuntimeException("User with id = " + id + " not found.");
        }
    }

    @Override
    @Transactional
    public UserShortDto createUser(UserRequestDto request) {

        if (request.getName().length() < 2 || request.getName().length() > 250 ||
                request.getEmail().length() < 6 || request.getEmail().length() > 254) {
            throw new ValidationException("Invalid name or email length");
        }

        if (userRepository.isEmailExists(request.getEmail())) {
            throw new DataAlreadyInUseException("Email " + request.getEmail() + " already in use.");
        }

        User user = userMapper.toUser(request);
        User saved = userRepository.save(user);
        return userMapper.toUserShortDto(saved);
    }

    @Override
    @Transactional
    public void deleteUser(long userId) {
        if (!userRepository.existsById(userId)) {
            throw new RuntimeException("User with id = " + userId + " not found.");
        }
        userRepository.deleteById(userId);
    }

    public List<UserShortDto> getUsersByIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }
        return userRepository.findAllById(ids).stream()
                .map(userMapper::toUserShortDto)
                .collect(Collectors.toList());
    }

    private User findById(long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User with id = " + userId + " not found."));
    }
}