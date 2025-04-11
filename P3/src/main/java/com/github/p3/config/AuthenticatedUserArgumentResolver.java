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

        AuthenticatedUser annotation = parameter.getParameterAnnotation(AuthenticatedUser.class);
        boolean required = annotation == null || annotation.required(); // 기본값 true

        var authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()
                || authentication.getPrincipal().equals("anonymousUser")) {
            if (required) {
                throw new RuntimeException("로그인이 필요합니다.");
            } else {
                return null; // 로그인 안 된 사용자 허용
            }
        }

        String email = authentication.getName();

        return userRepository.findByUserEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }
}