package teamfive.user.mapper;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.mapstruct.*;
import teamfive.user.dto.UserDto;
import teamfive.user.dto.UserRequestDto;
import teamfive.user.dto.UserUpdateDto;
import teamfive.user.model.User;

import java.util.List;
import java.util.stream.Collectors;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface UserMapper {

    User fromDto(UserRequestDto userDto);

    UserDto toDto(User user);

    default List<UserDto> toDtoList(List<User> users) {
        return users.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    default Page<UserDto> toDtoPage(Page<User> users) {
        List<UserDto> userDtos = users.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
        return new PageImpl<>(userDtos, users.getPageable(), users.getTotalElements());
    }

    @Mappings({
            @Mapping(source = "id", target = "id"),
            @Mapping(source = "name", target = "name"),
            @Mapping(source = "email", target = "email")
    })
    User fromDto(UserUpdateDto userDto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void userDtoRequestUpdate(UserUpdateDto userDto, @MappingTarget User user);
}
