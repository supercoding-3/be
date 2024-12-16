package com.github.p3.mapper;

import com.github.p3.dto.UserDto;
import com.github.p3.entity.User;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {
    public User toUserEntity(UserDto userDto) {
        User user = new User();
        user.setUserEmail(userDto.getUserEmail());
        user.setUserPassword(userDto.getUserPassword());
        user.setUserPassword(userDto.getUserPasswordConfirm());
        user.setUserNickname(userDto.getUserNickname());
        user.setUserPhone(userDto.getUserPhone());
        return user;
    }

    public UserDto toUserDto(User user) {
        UserDto userDto = new UserDto();
        userDto.setUserEmail(user.getUserEmail());
        userDto.setUserPassword(user.getUserPassword());
        userDto.setUserPasswordConfirm(user.getUserPassword());
        userDto.setUserNickname(user.getUserNickname());
        userDto.setUserPhone(user.getUserPhone());
        return userDto;
    }
}
