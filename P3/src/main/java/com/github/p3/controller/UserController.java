package com.github.p3.controller;

import com.github.p3.dto.UserDto;
import com.github.p3.service.UserService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
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
    public ResponseEntity<?> signup(@RequestBody UserDto userDto) {
        try {
            UserDto createdUser = userService.signup(userDto);
            return new ResponseEntity<>(createdUser, HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            // 잘못된 입력 데이터 예외 처리
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (RuntimeException e) {
            // 비즈니스 로직 예외 처리 (예: 중복 이메일, 닉네임 등)
            return new ResponseEntity<>(e.getMessage(), HttpStatus.CONFLICT);
        } catch (Exception e) {
            // 기타 서버 오류
            return new ResponseEntity<>("서버 내부 오류가 발생했습니다.", HttpStatus.INTERNAL_SERVER_ERROR);
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
            accessTokenCookie.setPath("/"); // 경로 설정
            accessTokenCookie.setMaxAge(30 * 60); // 30분

            response.addCookie(accessTokenCookie);

            return new ResponseEntity<>("로그인 성공", HttpStatus.OK);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.UNAUTHORIZED);
        }
    }

    // TODO: 로그아웃
    @PostMapping("/logout")
    public ResponseEntity<String> logout(HttpServletRequest request, HttpServletResponse response) {
        // Access Token 쿠키 만료
        Cookie accessTokenCookie = new Cookie("access_token", null);
        accessTokenCookie.setHttpOnly(true);
        accessTokenCookie.setPath("/");
        accessTokenCookie.setMaxAge(0); // 쿠키 만료 설정
        // Refresh Token 쿠키 만료
        Cookie refreshTokenCookie = new Cookie("refresh_token", null);
        refreshTokenCookie.setHttpOnly(true);
        refreshTokenCookie.setPath("/");
        refreshTokenCookie.setMaxAge(0); // 쿠키 만료 설정

        // 쿠키를 응답에 추가하여 클라이언트에서 쿠키 삭제
        response.addCookie(accessTokenCookie);
        response.addCookie(refreshTokenCookie);

        // DB에서 리프래시 토큰 삭제
        userService.removeRefreshToken(request);

        return new ResponseEntity<>("로그아웃 성공", HttpStatus.OK);
    }

    // 계정 비활성화
    @PatchMapping("/deactivate")
    public ResponseEntity<String> deactivateAccount(@RequestBody UserDto userDto) {
        try {
            userService.deactivateAccount(userDto.getUserEmail(), userDto.getUserPassword());
            return new ResponseEntity<>("계정이 비활성화되었습니다.", HttpStatus.OK);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }
}
