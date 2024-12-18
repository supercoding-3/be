package com.github.p3.service;

import com.github.p3.dto.UserDto;
import jakarta.servlet.http.HttpServletRequest;

import java.util.Map;

public interface UserService {
    // 회원가입
    UserDto signup(UserDto userDto);
    // 로그인
    Map<String, String> login(String userEmail, String userPassword);
    // 로그아웃 시 리프래시 토큰 삭제
    void removeRefreshToken(HttpServletRequest request);
    // 계정 비활성화
    void deactivateAccount(String userEmail, String userPassword);
}
