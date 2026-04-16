package ru.practicum.user.mapper;

import org.mapstruct.*;
import ru.practicum.dto.user.UserShortDto;
import ru.practicum.user.dto.UserDto;
import ru.practicum.user.dto.UserRequestDto;
import ru.practicum.user.model.User;

import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface UserMapper {

    @Mapping(target = "id", ignore = true)
    User toUser(UserRequestDto request);

    UserDto toUserDto(User user);

    UserShortDto toUserShortDto(User user);
}
