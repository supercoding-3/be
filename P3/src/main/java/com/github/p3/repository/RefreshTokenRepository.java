package com.github.p3.repository;

import com.github.p3.entity.RefreshToken;
import com.github.p3.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    Optional<RefreshToken> findByUser(User user);

    void deleteByUser(User user);
}
