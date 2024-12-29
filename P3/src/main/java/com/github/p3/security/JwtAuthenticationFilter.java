package com.github.p3.security;

import com.github.p3.entity.RefreshToken;
import com.github.p3.repository.RefreshTokenRepository;
import com.github.p3.util.CookieUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
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

        // CORS 헤더 추가
        response.setHeader("Access-Control-Allow-Origin", "http://localhost:3000");  // 클라이언트 도메인
        response.setHeader("Access-Control-Allow-Credentials", "true");  // 쿠키를 포함한 요청 허용
        response.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS, PATCH");  // 허용되는 HTTP 메서드
        response.setHeader("Access-Control-Allow-Headers", "Origin, Content-Type, X-Requested-With, Authorization");  // 허용되는 헤더들

        // CORS pre-flight 요청 처리 (OPTIONS 메서드)
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            response.setStatus(HttpServletResponse.SC_OK);
            return;
        }

        if (request.getRequestURI().equals("/api/user/login") ||
                request.getRequestURI().equals("/api/user/signup") ||
                request.getRequestURI().startsWith("/v3/api-docs") ||
                request.getRequestURI().startsWith("/swagger-ui") ||
                request.getRequestURI().equals("/swagger-ui/index.html")) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            // 쿠키에서 액세스 토큰 가져오기
            String accessToken = CookieUtil.getCookieValue(request, "access_token");
            System.out.println(accessToken + " 엑세스 토큰을 가져왔습니다.");
            System.out.println("Is token valid? " + jwtTokenProvider.validateToken(accessToken));

            if (accessToken != null && jwtTokenProvider.validateToken(accessToken)) {
                // 액세스 토큰 유효 => 이메일 추출
                String userEmail = jwtTokenProvider.extractUserEmail(accessToken);
                System.out.println("추출된 이메일: " + userEmail);  // 이메일 추출 전 로그

                if (userEmail != null) {
                    System.out.println(userEmail + " 이메일을 추출하였습니다.");
                    // 리프레시 토큰 찾기 (DB)
                    Optional<RefreshToken> refreshTokenOpt = refreshTokenRepository.findByUserEmail(userEmail);
                    // 해당 유저의 리프레시 토큰 처리 (예: 삭제)
                    refreshTokenOpt.ifPresent(refreshTokenRepository::delete);

                    // 액세스 토큰으로 인증 처리 (필요한 경우 SecurityContext 설정)
                    Authentication authentication = new UsernamePasswordAuthenticationToken(userEmail, null, new ArrayList<>());
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                } else {
                    response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "이메일을 추출할 수 없습니다.");
                    return;
                }
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

}