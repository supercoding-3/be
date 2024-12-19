package com.github.p3.repository;

import com.github.p3.entity.RefreshToken;
import com.github.p3.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    Optional<RefreshToken> findByUser(User user);
    void deleteByUser(User user);

    // 사용자 이메일로 RefreshToken 찾기
    @Query("SELECT rt FROM RefreshToken rt WHERE rt.user.userEmail = :email")
    Optional<RefreshToken> findByUserEmail(@Param("email") String email);
}