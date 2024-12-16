package com.github.p3.service;

import com.github.p3.dto.UserDto;

import java.util.Map;

public interface UserService {

    UserDto signup(UserDto userDto);

    Map<String, String> login(String userEmail, String userPassword);

    void deactivateAccount(String userEmail, String userPassword);
}
