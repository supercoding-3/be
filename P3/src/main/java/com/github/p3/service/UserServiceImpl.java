package com.github.p3.service;

import com.github.p3.dto.UserDto;
import com.github.p3.entity.User;
import com.github.p3.mapper.UserMapper;
import com.github.p3.repository.UserRepository;
import com.github.p3.security.JwtTokenProvider;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    @Override
    @Transactional
    public UserDto signup(UserDto userDto) {
        // 이메일 중복 체크
        if (userRepository.findByUserEmail(userDto.getUserEmail()).isPresent()) {
            throw new RuntimeException("이미 사용 중인 이메일입니다.");
        }
        // 닉네임 중복 체크
        if (userRepository.findByUserNickname(userDto.getUserNickname()).isPresent()) {
            throw new RuntimeException("이미 사용 중인 닉네임입니다.");
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
        User user = userRepository.findByUserEmail(userEmail).orElseThrow(() -> new RuntimeException("존재하지 않는 이메일입니다."));
        // 비밀번호 확인
        if (!passwordEncoder.matches(userPassword, user.getUserPassword())) {
            throw new RuntimeException("비밀번호가 일치하지 않습니다.");
        }

        // JWT 토큰 생성
        String accessToken = jwtTokenProvider.generateAccessToken(userEmail);
        String refreshToken = jwtTokenProvider.generateRefreshToken(userEmail);

        // 토큰 맵 생성
        Map<String, String> tokens = new HashMap<>();
        tokens.put("access_token", accessToken);
        tokens.put("refresh_token", refreshToken);

        return tokens;
    }
}
