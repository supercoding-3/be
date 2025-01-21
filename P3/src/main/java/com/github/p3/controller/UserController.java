package com.github.p3.controller;

import com.github.p3.dto.UserDto;
import com.github.p3.exception.CustomException;
import com.github.p3.service.UserService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


import java.util.Map;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
@Slf4j
public class UserController {

    private final UserService userService;

    // 회원가입
    @PostMapping("/signup")
    public ResponseEntity<?> signup(@RequestBody UserDto userDto) {
        try {
            UserDto createdUser = userService.signup(userDto);
            return new ResponseEntity<>(createdUser, HttpStatus.CREATED);
        } catch (CustomException e) {
            return new ResponseEntity<>(e.getMessage(), e.getErrorCode().getStatus());
        } catch (Exception e) {
            return new ResponseEntity<>("서버 내부 오류가 발생했습니다.", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // 로그인
    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody UserDto userDto, HttpServletResponse response) {
        try {
            log.info("로그인 시도: {}", userDto.getUserEmail());

            Map<String, String> tokens = userService.login(userDto.getUserEmail(), userDto.getUserPassword());

            // Access Token 쿠키 설정
            Cookie accessTokenCookie = new Cookie("access_token", tokens.get("access_token"));
            accessTokenCookie.setHttpOnly(true);
            accessTokenCookie.setPath("/");
            accessTokenCookie.setMaxAge(30 * 60);
            accessTokenCookie.setSecure(true);  // HTTPS에서만 쿠키 전송

            // SameSite 속성 수동 설정
            response.addHeader("Set-Cookie", "access_token=" + tokens.get("access_token") +
                    "; HttpOnly; Secure; Path=/; Max-Age=" + (30 * 60) + "; SameSite=None");

            log.info("로그인 성공: 이메일={}, 액세스 토큰={}", userDto.getUserEmail(), accessTokenCookie.getValue());

            return new ResponseEntity<>("로그인 성공", HttpStatus.OK);
        } catch (CustomException e) {
            log.warn("로그인 실패: {}", e.getMessage());
            return new ResponseEntity<>(e.getMessage(), e.getErrorCode().getStatus());
        } catch (Exception e) {
            log.error("로그인 중 서버 오류 발생", e);
            return new ResponseEntity<>("서버 내부 오류가 발생했습니다.", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // 로그아웃
    @PostMapping("/logout")
    public ResponseEntity<String> logout(HttpServletRequest request, HttpServletResponse response) {
        log.info("로그아웃 요청: {}", request.getRequestURI());  // 요청 URI 확인

        try {
            // Access Token 쿠키 만료
            Cookie accessTokenCookie = new Cookie("access_token", null);
            accessTokenCookie.setHttpOnly(true);
            accessTokenCookie.setPath("/");
            accessTokenCookie.setMaxAge(0);
            accessTokenCookie.setSecure(true);  // HTTPS에서만 쿠키 전송

            // SameSite 속성 수동 설정
            response.addHeader("Set-Cookie", "access_token=null; HttpOnly; Secure; Path=/; Max-Age=0; SameSite=None");

            // 리프래시 토큰 삭제 (DB)
            userService.removeRefreshToken(request);

            log.info("로그아웃 성공");
            return new ResponseEntity<>("로그아웃 성공", HttpStatus.OK);
        } catch (CustomException e) {
            log.warn("로그아웃 실패: {}", e.getMessage());
            return new ResponseEntity<>(e.getMessage(), e.getErrorCode().getStatus());
        } catch (Exception e) {
            log.error("로그아웃 중 서버 오류 발생", e);
            return new ResponseEntity<>("서버 내부 오류가 발생했습니다.", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // 계정 비활성화
    @PatchMapping("/deactivate")
    public ResponseEntity<String> deactivateAccount(@RequestBody UserDto userDto) {
        try {
            userService.deactivateAccount(userDto.getUserEmail(), userDto.getUserPassword());
            return new ResponseEntity<>("계정이 비활성화되었습니다.", HttpStatus.OK);
        } catch (CustomException e) {
            return new ResponseEntity<>(e.getMessage(), e.getErrorCode().getStatus());
        } catch (Exception e) {
            log.error("계정 비활성화 중 서버 오류 발생", e);
            return new ResponseEntity<>("서버 내부 오류가 발생했습니다.", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

}
