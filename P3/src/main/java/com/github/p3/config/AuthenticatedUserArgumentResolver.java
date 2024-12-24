package com.github.p3.config;

import com.github.p3.entity.User;
import com.github.p3.repository.UserRepository;

import lombok.AllArgsConstructor;
import org.springframework.core.MethodParameter;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;


@AllArgsConstructor
public class AuthenticatedUserArgumentResolver implements HandlerMethodArgumentResolver {


    private final UserRepository userRepository;


    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        // User 타입의 파라미터만 처리
        return parameter.getParameterType().equals(User.class);
    }

    @Override
    public Object resolveArgument(
            MethodParameter parameter,
            ModelAndViewContainer mavContainer,
            NativeWebRequest webRequest,
            WebDataBinderFactory binderFactory) throws Exception {
        // SecurityContext에서 현재 인증된 사용자의 이름(email) 가져오기
        String username = SecurityContextHolder.getContext().getAuthentication().getName();

        // UserRepository를 통해 사용자 정보 조회
        return userRepository.findByUserEmail(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }
}