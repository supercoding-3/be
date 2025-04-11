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
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenRepository refreshTokenRepository;

    private static final List<String> WHITELIST_URLS = List.of(
            "/api/user/login",
            "/api/user/check-login",
            "/api/user/signup",
            "/swagger-ui/index.html"
    );

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String uri = request.getRequestURI();

        return WHITELIST_URLS.contains(uri)
                || uri.startsWith("/api/products/all")
                || uri.matches("/api/products/\\d+")
                || uri.matches("/api/products/category/[A-Z]+")
                || uri.startsWith("/api/products/search")
                || uri.startsWith("/v3/api-docs")
                || uri.startsWith("/swagger-ui");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull FilterChain filterChain)
            throws ServletException, IOException {

        try {
            String accessToken = getTokenFromRequest(request);
            log.debug("요청 URI: {}, 추출된 액세스 토큰: {}", request.getRequestURI(), accessToken);

            if (accessToken != null) {
                handleAccessToken(request, response, accessToken);
                filterChain.doFilter(request, response);
            } else {
                log.warn("요청에 유효한 액세스 토큰이 포함되어 있지 않습니다.");
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "유효하지 않은 액세스 토큰.");
            }

        } catch (Exception e) {
            log.error("필터 처리 중 오류 발생: {}", e.getMessage(), e);
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "유효하지 않은 토큰입니다.");
        }
    }

    private void handleAccessToken(HttpServletRequest request, HttpServletResponse response, String accessToken) throws IOException {
        if (jwtTokenProvider.isTokenExpired(accessToken)) {
            log.info("액세스 토큰이 만료되었습니다. 리프레시 토큰 처리 시작...");
            processRefreshToken(request, response, accessToken);
        } else if (jwtTokenProvider.validateToken(accessToken)) {
            setAuthenticationFromAccessToken(accessToken);
        } else {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "액세스 토큰이 유효하지 않습니다.");
        }
    }

    private void processRefreshToken(HttpServletRequest request, HttpServletResponse response, String expiredAccessToken) throws IOException {
        String refreshToken = getRefreshTokenFromRequest(expiredAccessToken);

        if (refreshToken != null && jwtTokenProvider.validateToken(refreshToken)) {
            String newAccessToken = jwtTokenProvider.refreshAccessToken(refreshToken);
            storeAccessTokenInCookie(response, newAccessToken);
            setAuthenticationFromAccessToken(newAccessToken);
        } else {
            log.warn("리프레시 토큰이 유효하지 않습니다.");
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "리프레시 토큰이 유효하지 않습니다.");
        }
    }

    private void setAuthenticationFromAccessToken(String accessToken) {
        String userEmail = jwtTokenProvider.extractUserEmail(accessToken);
        if (userEmail != null) {
            Authentication authentication = new UsernamePasswordAuthenticationToken(
                    userEmail,
                    null,
                    new ArrayList<>()
            );
            SecurityContextHolder.getContext().setAuthentication(authentication);
            log.info("SecurityContext에 인증 정보가 설정되었습니다.");
        } else {
            log.warn("액세스 토큰에서 사용자 이메일을 추출할 수 없습니다.");
        }
    }

    private void storeAccessTokenInCookie(HttpServletResponse response, String accessToken) {
        Cookie cookie = new Cookie("access_token", accessToken);
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        response.addCookie(cookie);
        log.debug("새로운 액세스 토큰이 쿠키에 저장되었습니다.");
    }

    private String getTokenFromRequest(HttpServletRequest request) {
        return getTokenFromCookie(request).orElse(getTokenFromQueryParam(request));
    }

    private Optional<String> getTokenFromCookie(HttpServletRequest request) {
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if ("access_token".equals(cookie.getName())) {
                    return Optional.of(cookie.getValue());
                }
            }
        }
        return Optional.empty();
    }

    private String getTokenFromQueryParam(HttpServletRequest request) {
        String token = request.getParameter("access_token");
        return (token != null && !token.isEmpty()) ? token : null;
    }

    private String getRefreshTokenFromRequest(String accessToken) {
        String userEmail = jwtTokenProvider.extractUserEmail(accessToken);
        if (userEmail != null) {
            return refreshTokenRepository.findByUserEmail(userEmail)
                    .map(RefreshToken::getRefreshToken)
                    .orElse(null);
        }
        return null;
    }
}