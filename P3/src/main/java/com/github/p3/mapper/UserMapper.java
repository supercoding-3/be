package com.github.p3.mapper;

import com.github.p3.dto.UserDto;
import com.github.p3.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mapping(target = "userCreatedAt", source = "userCreatedAt")
    @Mapping(target = "userUpdatedAt", source = "userUpdatedAt")
    @Mapping(target = "userIsDeleted", source = "userIsDeleted")
    @Mapping(target = "profileImageUrl", source = "profileImageUrl")
    UserDto toUserDto(User user);

    @Mapping(target = "userIsDeleted", source = "userIsDeleted", defaultValue = "false")
    @Mapping(target = "userCreatedAt", ignore = true)
    @Mapping(target = "userUpdatedAt", ignore = true)
    @Mapping(target = "refreshToken", ignore = true)
    @Mapping(target = "userId", ignore = true)
    User toUserEntity(UserDto userDto);
}