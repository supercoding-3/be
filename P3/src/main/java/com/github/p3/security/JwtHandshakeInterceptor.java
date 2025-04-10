package com.github.p3.security;

import com.github.p3.entity.User;
import com.github.p3.repository.UserRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;
import lombok.RequiredArgsConstructor;

import java.net.URI;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtHandshakeInterceptor implements HandshakeInterceptor {

    private final UserRepository userRepository;

    @Value("${jwt.secret}")
    private String secretKey;

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                   WebSocketHandler wsHandler, Map<String, Object> attributes) throws Exception {
        String token = getTokenFromRequest(request);

        if (token != null && validateToken(token)) {
            Claims claims = getClaimsFromToken(token);
            String userEmail = claims.getSubject();

            Optional<User> userOptional = userRepository.findByUserEmail(userEmail);
            if (userOptional.isPresent()) {
                User user = userOptional.get();
                log.info("사용자 이메일: {}", userEmail);
                attributes.put("userEmail", userEmail);
                attributes.put("user", user);
                return true;
            }
        }
        response.setStatusCode(org.springframework.http.HttpStatus.FORBIDDEN);
        return false;
    }

    private String getTokenFromRequest(ServerHttpRequest request) {
        // 1. URL에서 access_token 가져오기
        String token = getQueryParam(request, "access_token");
        if (token != null) {
            return token; // 쿼리에 있으면 바로 반환
        }

        // 2. 쿼리에 없으면 Cookie에서 가져오기
        if (request.getHeaders().containsKey("Cookie")) {
            String cookieHeader = request.getHeaders().getFirst("Cookie");
            if (cookieHeader != null) {
                String[] cookies = cookieHeader.split(";");
                for (String cookie : cookies) {
                    cookie = cookie.trim();
                    int eqIdx = cookie.indexOf("=");
                    if (eqIdx > 0) { // '='가 포함된 경우만 처리
                        String key = cookie.substring(0, eqIdx).trim();
                        String value = cookie.substring(eqIdx + 1).trim();
                        if ("access_token".equals(key)) {
                            return value;
                        }
                    }
                }
            }
        }

        return null; // 토큰이 없으면 null 반환
    }

    private String getQueryParam(ServerHttpRequest request, String paramName) {
        URI uri = request.getURI();
        String query = uri.getQuery();
        if (query != null) {
            for (String param : query.split("&")) {
                String[] keyValue = param.split("=");
                if (keyValue.length == 2 && paramName.equals(keyValue[0])) {
                    return keyValue[1];
                }
            }
        }
        return null;
    }

    private boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(secretKey.getBytes())
                    .build()
                    .parseClaimsJws(token); // 토큰 파싱
            return true;
        } catch (io.jsonwebtoken.ExpiredJwtException e) {
            log.error("JWT 만료: {}", e.getMessage());
            return false;
        } catch (io.jsonwebtoken.MalformedJwtException e) {
            log.error("잘못된 JWT 형식: {}", e.getMessage());
            return false;
        } catch (io.jsonwebtoken.UnsupportedJwtException e) {
            log.error("지원되지 않는 JWT: {}", e.getMessage());
            return false;
        } catch (JwtException e) {
            log.error("JWT 검증 오류: {}", e.getMessage());
            return false;
        } catch (Exception e) {
            log.error("알 수 없는 오류: {}", e.getMessage());
            return false;
        }
    }

    private Claims getClaimsFromToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(secretKey.getBytes())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
                               WebSocketHandler wsHandler, Exception ex) {
        // 추가 로직이 필요 시 구현
    }
}