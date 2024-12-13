package com.github.p3.service;

import com.github.p3.dto.UserDto;
import com.github.p3.entity.User;
import com.github.p3.mapper.UserMapper;
import com.github.p3.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public UserDto signup(UserDto userDto) {
        // 이메일 중복 체크
        if (userRepository.findByUserEmail(userDto.getUserEmail()).isPresent()) {
            throw new RuntimeException("이미 사용 중인 이메일 입니다.");
        }
        // 닉네임 중복 체크
        if (userRepository.findByUserNickname(userDto.getUserNickname()).isPresent()) {
            throw new RuntimeException("이미 사용 중인 닉네임 입니다.");
        }

        User user = userMapper.toUserEntity(userDto);
        user = userRepository.save(user); // 사용자 저장
        return userMapper.toUserDto(user);
    }

    // TODO: login

    // TODO: logout

    // TODO: delete
}
