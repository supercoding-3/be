package com.github.p3.controller;

import com.github.p3.dto.UserDto;
import com.github.p3.service.UserService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    // 회원가입
    @PostMapping("/signup")
    public ResponseEntity<UserDto> signup(@RequestBody UserDto userDto) {
        try{
            UserDto createdUser = userService.signup(userDto);
            return new ResponseEntity<>(createdUser, HttpStatus.CREATED);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(null, HttpStatus.UNAUTHORIZED);
        }
    }

    // 로그인
    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody UserDto userDto, HttpServletResponse response) {
        try {
            Map<String, String> tokens = userService.login(userDto.getUserEmail(), userDto.getUserPassword());

            // Access Token 쿠키 설정
            Cookie accessTokenCookie = new Cookie("access_token", tokens.get("access_token"));
            accessTokenCookie.setHttpOnly(true); // HTTP-Only
            accessTokenCookie.setSecure(true); // 보안 적용
            accessTokenCookie.setPath("/"); // 경로 설정
            accessTokenCookie.setMaxAge(86400); // 1일

            // Refresh Token 쿠키 설정
            Cookie refreshTokenCookie = new Cookie("refresh_token", tokens.get("refresh_token"));
            refreshTokenCookie.setHttpOnly(true);
            refreshTokenCookie.setSecure(true);
            refreshTokenCookie.setPath("/");
            refreshTokenCookie.setMaxAge(604800); // 60초 * 60분 * 24시간 * 7일 = 7일

            response.addCookie(accessTokenCookie);
            response.addCookie(refreshTokenCookie);

            return new ResponseEntity<>("로그인 성공", HttpStatus.OK);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.UNAUTHORIZED);
        }
    }

    // 로그아웃
    @PostMapping("/logout")
    public ResponseEntity<String> logout(HttpServletResponse response) {
        // Access Token 쿠키 만료
        Cookie accessTokenCookie = new Cookie("access_token", null);
        accessTokenCookie.setHttpOnly(true);
        accessTokenCookie.setSecure(true);
        accessTokenCookie.setPath("/");
        response.addCookie(accessTokenCookie);
        accessTokenCookie.setMaxAge(0); // 쿠키 만료 처리

        // Refresh Token 쿠키 만료
        Cookie refreshTokenCookie = new Cookie("refresh_token", null);
        refreshTokenCookie.setHttpOnly(true);
        refreshTokenCookie.setSecure(true);
        refreshTokenCookie.setPath("/");
        response.addCookie(refreshTokenCookie);
        refreshTokenCookie.setMaxAge(0);

        response.addCookie(accessTokenCookie);
        response.addCookie(refreshTokenCookie);

        return new ResponseEntity<>("로그아웃 성공", HttpStatus.OK);
    }

    // 계정 비활성화
    @PatchMapping("/deactivate")
    public ResponseEntity<String> deactivateAccount(@RequestBody UserDto userDto) {
        try {
            userService.deactivateAccount(userDto.getUserEmail());
            return new ResponseEntity<>("계정이 바활성화되었습니다.", HttpStatus.OK);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }
}
