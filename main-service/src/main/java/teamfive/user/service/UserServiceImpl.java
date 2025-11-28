package teamfive.user.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import teamfive.exception.DuplicatedException;
import teamfive.exception.NotFoundException;
import teamfive.user.dto.UserDto;
import teamfive.user.dto.UserRequestDto;
import teamfive.user.dto.UserUpdateDto;
import teamfive.user.mapper.UserMapper;
import teamfive.user.model.User;
import teamfive.user.repository.UserRepository;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;


    @Override
    public UserDto get(Long id) {
        log.info("Начало поиска пользователя с ID: {}", id);
        Optional<User> userOptional = userRepository.findById(id);
        log.info("Пользователь с ID {} найден: {}", id, userOptional.isPresent());

        UserDto userDto;
        if (userOptional.isPresent()) {
            userDto = userMapper.toDto(userOptional.get());
            log.info("Преобразование пользователя с ID {} в DTO успешно завершено.", id);
        } else {
            log.error("Пользователь с ID {} не найден.", id);
            throw new NotFoundException("Пользователь с таким id не найден");
        }
        return userDto;
    }


    @Override
    public List<UserDto> get(List<Long> ids) {
        log.info("Начало поиска пользователей по спискам ID: {}", ids);
        List<User> users = userRepository.findByIdIn(ids);
        log.info("Найдено {} пользователей.", users.size());

        List<UserDto> userDtos = users.stream()
                .map(userMapper::toDto)
                .toList();
        log.info("Преобразование найденных пользователей (list) в DTO успешно завершено.");

        return userDtos;
    }


    @Override
    public Page<UserDto> get(List<Long> ids, Pageable pageable) {
        log.info("Начало поиска пользователей по спискам ID: {} с пагинацией: {}", ids, pageable);
        Page<User> users;

        if (ids == null || ids.isEmpty()) {
            log.info("Список ID пуст, выполняется поиск всех пользователей с пагинацией.");
            users = userRepository.findAll(pageable);
        } else {
            log.info("Поиск пользователей по указанным ID с пагинацией.");
            users = userRepository.findByIdIn(ids, pageable);
        }

        Page<UserDto> userDtos = userMapper.toDtoPage(users);
        log.info("Преобразование найденных пользователей (Page) в DTO успешно завершено.");

        return userDtos;
    }


    @Override
    @Transactional
    public UserDto create(UserRequestDto requestDto) {
        log.debug("Добавление пользователя с данными: {}", requestDto);

        User user = userMapper.fromDto(requestDto);
        if (checkEmailExist(user.getEmail())) {
            throw new DuplicatedException("Пользователь с таким email уже существует");
        }

        log.debug("Сохранение пользователя в базу данных");
        User userSaved = userRepository.save(user);
        userRepository.flush();
        log.info("Пользователь успешно сохранён с ID: {}", userSaved.getId());

        return userMapper.toDto(userSaved);
    }


    @Override
    @Transactional
    public void delete(Long id) {
        log.info("Начало удаления пользователя с ID: {}", id);
        userRepository.deleteById(id);
        log.info("Пользователь с ID {} успешно удалён", id);
        userRepository.flush();
        log.info("Транзакция успешно завершена");
    }


    @Override
    public boolean existsById(Long id) {
        log.debug("Проверка существования пользователя с ID: {}", id);
        boolean exists = userRepository.existsById(id);
        log.debug("Пользователь с ID {} существует: {}", id, exists);
        return exists;
    }


    @Override
    public boolean checkEmailExist(String email) {
        log.debug("Проверка существования пользователя по email: {}", email);
        boolean exists = userRepository.existsByEmail(email);
        log.debug("Пользователь с email {} существует: {}", email, exists);
        return exists;
    }


    @Override
    @Transactional
    public UserDto patch(UserUpdateDto userNew) {
        log.info("Пользователь до обновления: {}", userNew.toString());

        if (!userRepository.existsById(userNew.getId())) {
            throw new NotFoundException("Обновление невозможно. Пользователь не найден");
        }

        if (userNew.getEmail() != null && checkEmailExist(userNew.getEmail())) {
            throw new DuplicatedException("Обновление невозможно. Email уже используется");
        }

        User user = userMapper.fromDto(userNew);
        userMapper.userDtoRequestUpdate(userNew, user);
        User updatedUser = userRepository.save(user);
        UserDto updatedUserDto = userMapper.toDto(updatedUser);

        log.info("Пользователь после обновления: {}", updatedUserDto);

        return updatedUserDto;
    }
}