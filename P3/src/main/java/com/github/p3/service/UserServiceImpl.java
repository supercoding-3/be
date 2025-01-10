package com.github.p3.service;

import com.github.p3.dto.UserDto;
import com.github.p3.entity.RefreshToken;
import com.github.p3.entity.User;
import com.github.p3.exception.CustomException;
import com.github.p3.exception.ErrorCode;
import com.github.p3.mapper.UserMapper;
import com.github.p3.repository.RefreshTokenRepository;
import com.github.p3.repository.UserRepository;
import com.github.p3.security.JwtTokenProvider;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenRepository refreshTokenRepository;

    @Override
    public UserDto signup(UserDto userDto) {
        // 이메일 형식 검증
        if (!UserServiceRegexImpl.isValidEmail(userDto.getUserEmail())) {
            throw new CustomException(ErrorCode.INVALID_EMAIL);
        }

        // 닉네임 형식 검증
        if (!UserServiceRegexImpl.isValidNickname(userDto.getUserNickname())) {
            throw new CustomException(ErrorCode.INVALID_NICKNAME);
        }

        // 비밀번호 형식 검증
        if (!UserServiceRegexImpl.isValidPassword(userDto.getUserPassword())) {
            throw new CustomException(ErrorCode.INVALID_PASSWORD);
        }

        // 이메일 중복 체크
        Optional<User> existingUser = userRepository.findByUserEmail(userDto.getUserEmail());
        if (existingUser.isPresent()) {
            if (existingUser.get().getUserIsDeleted()) {
                throw new CustomException(ErrorCode.ACCOUNT_DEACTIVATED);
            } else {
                throw new CustomException(ErrorCode.DUPLICATE_EMAIL);
            }
        }

        // 닉네임 중복 체크
        Optional<User> existingNickname = userRepository.findByUserNickname(userDto.getUserNickname());
        if (existingNickname.isPresent()) {
            if (existingNickname.get().getUserIsDeleted()) {
                throw new CustomException(ErrorCode.ACCOUNT_DEACTIVATED);
            } else {
                throw new CustomException(ErrorCode.DUPLICATE_NICKNAME);
            }
        }

        User user = userMapper.toUserEntity(userDto);
        user.setUserPassword(passwordEncoder.encode(userDto.getUserPassword())); // 비밀번호 암호화
        user = userRepository.save(user); // 사용자 저장
        return userMapper.toUserDto(user);
    }

    @Override
    @Transactional
    public Map<String, String> login(String userEmail, String userPassword) {
        // 이메일로 사용자 조회
        User user = userRepository.findByUserEmail(userEmail)
                .orElseThrow(() -> new CustomException(ErrorCode.EMAIL_NOT_FOUND));

        // 비활성화된 계정 체크
        if (user.getUserIsDeleted()) {
            throw new CustomException(ErrorCode.ACCOUNT_DEACTIVATED);
        }
        // 비밀번호 확인
        if (!passwordEncoder.matches(userPassword, user.getUserPassword())) {
            throw new CustomException(ErrorCode.INCORRECT_PASSWORD);
        }

        // JWT 토큰 생성
        String accessToken = jwtTokenProvider.generateAccessToken(userEmail);
        String refreshToken = jwtTokenProvider.generateRefreshToken(userEmail);

        // RefreshToken DB에 저장
        saveRefreshToken(userEmail, refreshToken);

        Map<String, String> tokens = new HashMap<>();
        tokens.put("access_token", accessToken);

        return tokens;
    }

    @Transactional
    public void saveRefreshToken(String userEmail, String refreshToken) {
        // 사용자 조회
        User user = userRepository.findByUserEmail(userEmail)
                .orElseThrow(() -> new CustomException(ErrorCode.EMAIL_NOT_FOUND));

        // 기존 리프래시 토큰이 있는 경우 업데이트
        Optional<RefreshToken> existingToken = refreshTokenRepository.findByUser(user);
        if (existingToken.isPresent()) {
            // 기존 리프래시 토큰 업데이트
            RefreshToken token = existingToken.get();
            token.setRefreshToken(refreshToken); // 리프래시 토큰 값 갱신
            token.setCreatedAt(LocalDateTime.now());
            token.setExpiresAt(LocalDateTime.now().plusDays(7)); // 만료일 갱신
            refreshTokenRepository.save(token);
        } else {
            // 새로운 리프래시 토큰 저장
            RefreshToken newToken = new RefreshToken();
            newToken.setUser(user);
            newToken.setRefreshToken(refreshToken);
            newToken.setCreatedAt(LocalDateTime.now()); // 생성일 설정
            newToken.setExpiresAt(LocalDateTime.now().plusDays(7)); // 만료일 설정
            refreshTokenRepository.save(newToken);
        }
    }

    @Override
    @Transactional
    public void removeRefreshToken(HttpServletRequest request) {
        String accessToken = Arrays.stream(request.getCookies())
                .filter(cookie -> "access_token".equals(cookie.getName()))
                .map(Cookie::getValue)
                .findFirst()
                .orElse(null);

        if (accessToken != null) {
            String userEmail = jwtTokenProvider.extractUserEmail(accessToken);
            User user = userRepository.findByUserEmail(userEmail).orElseThrow(() -> new CustomException(ErrorCode.EMAIL_NOT_FOUND));
            refreshTokenRepository.deleteByUser(user);
        }
    }

    @Override
    @Transactional
    public void deactivateAccount(String userEmail, String userPassword) {
        User user = userRepository.findByUserEmail(userEmail).orElseThrow(() -> new CustomException(ErrorCode.EMAIL_NOT_FOUND));

        if (user.getUserIsDeleted()) {
            refreshTokenRepository.deleteByUser(user);
            throw new CustomException(ErrorCode.ACCOUNT_DEACTIVATED);
        }

        if (!passwordEncoder.matches(userPassword, user.getUserPassword())) {
            throw new CustomException(ErrorCode.INCORRECT_PASSWORD);
        }

        user.setUserIsDeleted(true);
        userRepository.save(user);
    }
}