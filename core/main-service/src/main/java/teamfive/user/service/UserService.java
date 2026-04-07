package teamfive.user.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import teamfive.user.dto.UserDto;
import teamfive.user.dto.UserRequestDto;
import teamfive.user.dto.UserUpdateDto;

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