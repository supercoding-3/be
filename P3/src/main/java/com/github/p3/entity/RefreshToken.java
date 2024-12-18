package com.github.p3.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "refresh_tokens")
@Slf4j
public class RefreshToken {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "token_id")
    private Long tokenId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "refresh_token", nullable = false)
    private String refreshToken;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    // RefreshToken 생성 시 만료일을 자동으로 설정
    @PrePersist
    public void setExpiresAt() {
        if (expiresAt == null) {
            this.expiresAt = LocalDateTime.now().plusDays(7); // 현재 시간 기준 7일 후
        }
    }

    // RefreshToken 갱신 시 createdAt 갱신
    @PreUpdate
    public void updateCreatedAt() {
        this.createdAt = LocalDateTime.now();
    }
}
