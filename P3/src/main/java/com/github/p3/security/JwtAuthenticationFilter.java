package com.github.p3.security;

import com.github.p3.entity.RefreshToken;
import com.github.p3.repository.RefreshTokenRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenRepository refreshTokenRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull FilterChain filterChain)
            throws IOException, ServletException {

        // 로그인, 회원가입, 로그아웃 시 필터를 건너뛰기
        if (request.getRequestURI().equals("/api/user/login") || request.getRequestURI().equals("/api/user/signup")) {
            filterChain.doFilter(request, response);  // 로그인, 회원가입, 로그아웃이면 필터를 건너뛰고 진행
            return;
        }

        try {
            // 쿠키에서 액세스 토큰 가져오기
            String accessToken = getTokenFromCookies(request);

            if (accessToken != null && jwtTokenProvider.validateToken(accessToken)) {
                // 액세스 토큰 유효 => 이메일 추출
                String userEmail = jwtTokenProvider.extractUserEmail(accessToken);
                // 리프레시 토큰 찾기 (DB)
                Optional<RefreshToken> refreshTokenOpt = refreshTokenRepository.findByUserEmail(userEmail);
                // 해당 유저의 리프레시 토큰 처리 (예: 삭제)
                refreshTokenOpt.ifPresent(refreshTokenRepository::delete);

                // 액세스 토큰으로 인증 처리 (필요한 경우 SecurityContext 설정)
                Authentication authentication = new UsernamePasswordAuthenticationToken(userEmail, null, new ArrayList<>());
                SecurityContextHolder.getContext().setAuthentication(authentication);
            } else {
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "유효하지 않은 액세스 토큰");
                return;
            }
        } catch (Exception e) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "유효하지 않은 토큰입니다.");
            return;
        }

        filterChain.doFilter(request, response);  // 필터 체인 진행
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
}