package com.github.p3.dto;

import lombok.*;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RefreshTokenDto {
    private String refreshToken;
    private LocalDateTime expiresAt;
}
