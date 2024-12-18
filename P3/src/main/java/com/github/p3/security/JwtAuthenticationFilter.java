package com.github.p3.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtTokenProvider jwtTokenProvider;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws IOException, ServletException {
        // 회원가입과 첫 로그인 시 토큰 검사 안함
        if (request.getRequestURI().equals("/api/user/login") || request.getRequestURI().equals("/api/user/signup")) {
            filterChain.doFilter(request, response);  // 회원가입이면 필터를 건너뛰고 그대로 진행
            return;
        }

        try {
            // 쿠키에서 액세스 토큰 가져오기
            String accessToken = getTokenFromCookies(request);

            if (accessToken != null && jwtTokenProvider.validateToken(accessToken)) {
                // 액세스 토큰 유효
                String userEmail = jwtTokenProvider.extractUserEmail(accessToken);
                Authentication authentication = new UsernamePasswordAuthenticationToken(userEmail, null, new ArrayList<>());
                SecurityContextHolder.getContext().setAuthentication(authentication);
            } else {
                // 액세스 토큰이 만료된 경우, 리프레시 토큰으로 새로운 액세스 토큰을 발급
                String refreshToken = getRefreshTokenFromCookies(request);

                if (refreshToken != null && jwtTokenProvider.validateToken(refreshToken)) {
                    // 리프레시 토큰 유효 => 새로운 액세스 토큰 생성
                    String newAccessToken = jwtTokenProvider.refreshAccessToken(refreshToken);

                    // 새로운 액세스 토큰을 쿠키에 설정
                    response.addCookie(createAccessTokenCookie(newAccessToken));

                    // 사용자 인증
                    String userEmail = jwtTokenProvider.extractUserEmail(refreshToken);
                    Authentication authentication = new UsernamePasswordAuthenticationToken(userEmail, null, new ArrayList<>());
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                } else {
                    response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "리프래시 토큰이 유효하지 않습니다.");
                    return;
                }
            }
        } catch (Exception e) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "유효하지 않은 토큰입니다.");
            return;
        }

        filterChain.doFilter(request, response);
    }

    // 쿠키에서 액세스 토큰 가져오기
    private String getTokenFromCookies(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("access_token".equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }

    // 쿠키에서 리프레시 토큰 가져오기
    private String getRefreshTokenFromCookies(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("refresh_token".equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }

    // 새로운 액세스 토큰을 쿠키에 추가하는 메서드
    private Cookie createAccessTokenCookie(String accessToken) {
        Cookie accessTokenCookie = new Cookie("access_token", accessToken);
        accessTokenCookie.setHttpOnly(true);
        accessTokenCookie.setPath("/"); // 경로 설정
        accessTokenCookie.setMaxAge(30 * 60); // 30분 (컨트롤러와 동일)
        return accessTokenCookie;
    }
}