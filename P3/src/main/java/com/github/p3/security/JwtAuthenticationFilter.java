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

        // 로그인, 회원가입, Swagger 관련 URL은 필터에서 제외
        if (request.getRequestURI().equals("/api/user/login") ||
                request.getRequestURI().equals("/api/user/signup") ||
                request.getRequestURI().startsWith("/v3/api-docs") ||
                request.getRequestURI().startsWith("/swagger-ui") ||
                request.getRequestURI().equals("/swagger-ui/index.html")) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            // 요청에서 액세스 토큰 가져오기
            String accessToken = getTokenFromRequest(request);

            if (accessToken != null && jwtTokenProvider.validateToken(accessToken)) {
                // 액세스 토큰이 유효하다면, 이메일 추출
                String userEmail = jwtTokenProvider.extractUserEmail(accessToken);

                if (userEmail != null) {
                    // 유저의 리프레시 토큰을 DB에서 찾아서 삭제하지 않음
                    Optional<RefreshToken> refreshTokenOpt = refreshTokenRepository.findByUserEmail(userEmail);

                    // 리프레시 토큰이 존재하고 만료되지 않았으면 삭제하지 않음
                    refreshTokenOpt.ifPresent(refreshToken -> {
                        if (!jwtTokenProvider.validateToken(refreshToken.getRefreshToken())) {
                            refreshTokenRepository.delete(refreshToken);  // 만료된 리프레시 토큰만 삭제
                        }
                    });

                    // 인증 처리: SecurityContext에 인증 정보 설정
                    Authentication authentication = new UsernamePasswordAuthenticationToken(userEmail, null, new ArrayList<>());
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                } else {
                    response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "이메일을 추출할 수 없습니다.");
                    return;
                }
            } else if (accessToken != null && !jwtTokenProvider.validateToken(accessToken)) {
                // 액세스 토큰이 만료되었을 경우, 리프레시 토큰을 사용하여 갱신
                String refreshToken = getRefreshTokenFromRequest(request);

                if (refreshToken != null && jwtTokenProvider.validateToken(refreshToken)) {
                    String newAccessToken = jwtTokenProvider.refreshAccessToken(refreshToken);
                    response.setHeader("Authorization", "Bearer " + newAccessToken);  // 새로운 액세스 토큰을 헤더에 설정
                } else {
                    response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "리프레시 토큰이 유효하지 않습니다.");
                    return;
                }
            } else {
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "유효하지 않은 액세스 토큰.");
                return;
            }
        } catch (Exception e) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "유효하지 않은 토큰.");
            return;
        }

        filterChain.doFilter(request, response);  // 필터 체인 진행
    }

    // 요청에서 액세스 토큰을 가져오는 메서드 (쿠키 또는 쿼리 파라미터에서)
    private String getTokenFromRequest(HttpServletRequest request) {
        String token = getTokenFromCookie(request);
        if (token != null) {
            return token;
        }

        // 쿠키에서 토큰이 없으면 쿼리 파라미터에서 가져옴
        return getTokenFromQueryParam(request);
    }

    // 쿠키에서 액세스 토큰을 가져오는 메서드
    private String getTokenFromCookie(HttpServletRequest request) {
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if ("access_token".equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }

    // 쿼리 파라미터에서 액세스 토큰을 가져오는 메서드
    private String getTokenFromQueryParam(HttpServletRequest request) {
        String token = request.getParameter("access_token");
        if (token != null && !token.isEmpty()) {
            return token;
        }
        return null;
    }

    // 요청에서 리프레시 토큰을 가져오는 메서드
    private String getRefreshTokenFromRequest(HttpServletRequest request) {
        String refreshToken = request.getHeader("Refresh-Token");
        if (refreshToken != null && !refreshToken.isEmpty()) {
            return refreshToken;
        }

        return null;
    }
}