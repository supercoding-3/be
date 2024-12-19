package com.github.p3.mapper;

import com.github.p3.dto.UserDto;
import com.github.p3.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserMapper {
    @Mapping(target = "userIsDeleted", source = "userIsDeleted", defaultValue = "false")
    UserDto toUserDto(User user);

    @Mapping(target = "userIsDeleted", source = "userIsDeleted", defaultValue = "false")
    User toUserEntity(UserDto userDto);
}
