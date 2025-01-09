package com.github.p3.mapper;

import com.github.p3.dto.UserDto;
import com.github.p3.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserMapper {

    // User 엔티티를 UserDto로 변환
    @Mapping(target = "userIsDeleted", source = "userIsDeleted", defaultValue = "false")
    UserDto toUserDto(User user);

    // UserDto를 User 엔티티로 변환
    @Mapping(target = "userIsDeleted", source = "userIsDeleted", defaultValue = "false")
    @Mapping(target = "userCreatedAt", ignore = true)
    @Mapping(target = "userUpdatedAt", ignore = true)
    @Mapping(target = "refreshToken", ignore = true)
    @Mapping(target = "userId", ignore = true)
    User toUserEntity(UserDto userDto);
}