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

    // 액세스 토큰 생성
    public String generateAccessToken(String userEmail) {
        return generateToken(userEmail, accessTokenExpirationTime);
    }

    // 리프래시 토큰 생성
    public String generateRefreshToken(String userEmail) {
        return generateToken(userEmail, refreshTokenExpirationTime);
    }

    // 토큰 생성 메서드
    private String generateToken(String userEmail, long expirationTime) {
        Key key = Keys.hmacShaKeyFor(secretKey.getBytes()); // secretKey로 서명

        return Jwts.builder()
                .setSubject(userEmail)
                .setIssuedAt(new Date()) // 발행시간
                .setExpiration(new Date(System.currentTimeMillis() + expirationTime)) // 만료시간
                .signWith(key, SignatureAlgorithm.HS256) // 서명 알고리즘 설정
                .compact(); // 토큰 생성
    }

    // 토큰에서 이메일 추출 (JWT 검증)
    public String extractUserEmail(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(Keys.hmacShaKeyFor(secretKey.getBytes()))
                .build()
                .parseClaimsJws(token)
                .getBody();

        return claims.getSubject();
    }

    // 토큰 유효성 검사
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(Keys.hmacShaKeyFor(secretKey.getBytes())) // 서명 검증
                    .build()
                    .parseClaimsJws(token); // 토큰 검증
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            System.out.println("토큰 검증 오류: " + e.getMessage());
            return false;
        }
    }

    // 리프래시 토큰으로 액세스 토큰 갱신
    public String refreshAccessToken(String refreshToken) {
        // 리프래시 토큰 검증
        if (!validateToken(refreshToken)) {
            throw new RuntimeException("무효한 refresh token 입니다.");
        }
        // 리프래시 토큰에서 사용자 이메일 추출
        String userEmail = extractUserEmail(refreshToken);
        // 새로운 액세스 토큰 발급
        return generateAccessToken(userEmail);
    }

}