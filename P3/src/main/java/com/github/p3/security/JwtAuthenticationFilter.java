package com.github.p3.security;

import com.github.p3.entity.RefreshToken;
import com.github.p3.repository.RefreshTokenRepository;
import io.jsonwebtoken.ExpiredJwtException;
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
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenRepository refreshTokenRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull FilterChain filterChain)
            throws IOException, ServletException {

        // 특정 URL은 필터에서 제외
        if (isExcludedUrl(request.getRequestURI())) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            String accessToken = getTokenFromRequest(request);
            log.debug("요청 URI: {}, 추출된 액세스 토큰: {}", request.getRequestURI(), accessToken);

            if (accessToken != null) {
                processAccessToken(request, response, accessToken);
            } else {
                log.warn("요청에 유효한 액세스 토큰이 포함되어 있지 않습니다.");
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "유효하지 않은 액세스 토큰.");
                return;
            }

            filterChain.doFilter(request, response);
        } catch (Exception e) {
            log.error("필터 처리 중 오류 발생: {}", e.getMessage(), e);
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "유효하지 않은 토큰입니다.");
        }
    }

    // 특정 URL 필터링 제외 여부 확인
    private boolean isExcludedUrl(String requestUri) {
        return requestUri.matches("(/api/user/(login|signup)|/api/products/(all|\\{id}|category/\\{category})|/v3/api-docs|/swagger-ui(/index.html)?)");
    }

    // 액세스 토큰 처리
    private void processAccessToken(HttpServletRequest request, HttpServletResponse response, String accessToken) throws IOException {
        try {
            if (jwtTokenProvider.validateToken(accessToken)) {
                setAuthenticationFromAccessToken(accessToken);
            }
        } catch (ExpiredJwtException e) {
            log.info("액세스 토큰이 만료되었습니다. 리프레시 토큰 처리 시작...");
            String refreshToken = getRefreshTokenFromRequest(request);

            if (refreshToken != null && jwtTokenProvider.validateToken(refreshToken)) {
                log.info("유효한 리프레시 토큰입니다. 새 액세스 토큰을 생성합니다.");
                String newAccessToken = jwtTokenProvider.refreshAccessToken(refreshToken);
                storeNewAccessTokenInCookie(response, newAccessToken);
                setAuthenticationFromAccessToken(newAccessToken);
            } else {
                log.warn("유효하지 않은 리프레시 토큰입니다.");
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "리프레시 토큰이 유효하지 않습니다.");
                throw new SecurityException("리프레시 토큰이 유효하지 않습니다.");
            }
        }
    }

    // 새 액세스 토큰을 쿠키에 저장
    private void storeNewAccessTokenInCookie(HttpServletResponse response, String newAccessToken) {
        Cookie newAccessTokenCookie = new Cookie("access_token", newAccessToken);
        newAccessTokenCookie.setHttpOnly(true);
        newAccessTokenCookie.setPath("/");
        response.addCookie(newAccessTokenCookie);
        log.debug("새로운 액세스 토큰이 쿠키에 저장되었습니다.");
    }

    // SecurityContext에 인증 정보 설정
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

    // 요청에서 액세스 토큰 가져오기
    private String getTokenFromRequest(HttpServletRequest request) {
        return getTokenFromCookie(request).orElse(getTokenFromQueryParam(request));
    }

    // 쿠키에서 액세스 토큰 가져오기
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

    // 쿼리 파라미터에서 액세스 토큰 가져오기
    private String getTokenFromQueryParam(HttpServletRequest request) {
        String token = request.getParameter("access_token");
        return (token != null && !token.isEmpty()) ? token : null;
    }

    // 요청에서 리프레시 토큰 가져오기
    private String getRefreshTokenFromRequest(HttpServletRequest request) {
        String accessToken = getTokenFromRequest(request);
        if (accessToken == null) {
            log.warn("요청에 액세스 토큰이 포함되어 있지 않습니다.");
            return null;
        }

        String userEmail = jwtTokenProvider.extractUserEmail(accessToken);
        if (userEmail != null) {
            return refreshTokenRepository.findByUserEmail(userEmail)
                    .map(RefreshToken::getRefreshToken)
                    .orElse(null);
        }
        log.warn("액세스 토큰에서 이메일을 추출할 수 없습니다.");
        return null;
    }
}