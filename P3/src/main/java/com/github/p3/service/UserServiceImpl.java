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
import java.util.Optional;

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
        Optional<User> existingUser = userRepository.findByUserEmail(userDto.getUserEmail());
        if (existingUser.isPresent()) {
            if (existingUser.get().getUserIsDeleted()) {
                throw new RuntimeException("이 이메일은 비활성화된 계정입니다. 계정을 복구하거나 다른 이메일을 사용하세요.");
            } else {
                throw new RuntimeException("이미 사용 중인 이메일입니다.");
            }
        }

        // 닉네임 중복 체크
        Optional<User> existingNickname = userRepository.findByUserNickname(userDto.getUserNickname());
        if (existingNickname.isPresent()) {
            if (existingNickname.get().getUserIsDeleted()) {
                throw new RuntimeException("이 닉네임은 이미 비활성화된 계정으로 존재합니다. 계정을 복구하거나 다른 닉네임을 사용하세요.");
            } else {
                throw new RuntimeException("이미 사용 중인 닉네임입니다.");
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
        User user = userRepository.findByUserEmail(userEmail).orElseThrow(() -> new RuntimeException("존재하지 않는 이메일입니다."));

        // 비활성화된 계정 체크
        if (user.getUserIsDeleted()) {
            throw new RuntimeException("비활성화된 계정입니다. 계정을 복구하거나 다른 이메일을 사용하세요.");
        }
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

    @Override
    @Transactional
    public void deactivateAccount(String userEmail, String userPassword) {
        User user = userRepository.findByUserEmail(userEmail).orElseThrow(() -> new RuntimeException("존재하지 않는 이메일입니다."));

        if (user.getUserIsDeleted()) {
            throw new RuntimeException("이미 비활성화된 계정입니다.");
        }

        if (!passwordEncoder.matches(userPassword, user.getUserPassword())) {
            throw new RuntimeException("비밀번호가 일치하지 않습니다.");
        }

        user.setUserIsDeleted(true);
        userRepository.save(user);
    }
}
