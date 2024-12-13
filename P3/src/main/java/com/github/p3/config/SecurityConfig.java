package com.github.p3.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable()) // CSRF 보호 비활성화
                .authorizeHttpRequests(authorizeRequests ->
                        authorizeRequests
                                .requestMatchers(
                                        "/api/user/signup"
                                ).permitAll()
                                .anyRequest().authenticated()
                )
                .formLogin(formLogin -> formLogin.disable()) // 기본 로그인 폼 비활성화
                .httpBasic(httpBasic -> httpBasic.disable()); // 기본 HTTP 인증 비활성화 (JWT 사용 시)

        return http.build(); // 설정을 빌드하여 반환
    }
}


