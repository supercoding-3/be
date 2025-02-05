package com.github.p3.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;

@Component
public class JwtTokenProvider {

    @Value("${jwt.secret}")
    private String secretKey;

    @Value("${jwt.access-token-expiration-time}")
    private long accessTokenExpirationTime;

    @Value("${jwt.refresh-token-expiration-time}")
    private long refreshTokenExpirationTime;

    private Key getSigningKey() {
        return Keys.hmacShaKeyFor(secretKey.getBytes());
    }

    // 액세스 토큰 생성
    public String generateAccessToken(String userEmail) {
        return generateToken(userEmail, accessTokenExpirationTime);
    }

    // 리프레시 토큰 생성
    public String generateRefreshToken(String userEmail) {
        return generateToken(userEmail, refreshTokenExpirationTime);
    }

    // 토큰 생성 메서드
    private String generateToken(String userEmail, long expirationTime) {
        return Jwts.builder()
                .setSubject(userEmail)
                .setIssuedAt(new Date()) // 발행시간
                .setExpiration(new Date(System.currentTimeMillis() + expirationTime)) // 만료시간
                .signWith(getSigningKey(), SignatureAlgorithm.HS256) // 서명 알고리즘 설정
                .compact(); // 토큰 생성
    }

    // 토큰에서 이메일 추출 (JWT 검증)
    public String extractUserEmail(String token) {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
            return claims.getSubject();
        } catch (ExpiredJwtException e) {
            return e.getClaims().getSubject(); // 만료된 경우에도 이메일 반환
        } catch (JwtException | IllegalArgumentException e) {
            return null;
        }
    }

    // 토큰 만료 여부 확인
    public boolean isTokenExpired(String token) {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
            return claims.getExpiration().before(new Date());
        } catch (ExpiredJwtException e) {
            return true; // ✅ 만료된 경우 true 반환
        } catch (JwtException | IllegalArgumentException e) {
            return false; // ✅ 다른 예외 발생 시 false
        }
    }

    // 토큰 유효성 검사 (만료된 경우도 허용)
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(getSigningKey()) // 서명 검증
                    .build()
                    .parseClaimsJws(token); // 토큰 검증
            return true;
        } catch (ExpiredJwtException e) {
            return true; // ✅ 만료된 토큰도 검증 자체는 성공으로 처리
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    // 리프레시 토큰으로 액세스 토큰 갱신
    public String refreshAccessToken(String refreshToken) {
        if (!validateToken(refreshToken) || isTokenExpired(refreshToken)) {
            throw new RuntimeException("무효한 refresh token 입니다.");
        }
        String userEmail = extractUserEmail(refreshToken);
        return generateAccessToken(userEmail);
    }
}