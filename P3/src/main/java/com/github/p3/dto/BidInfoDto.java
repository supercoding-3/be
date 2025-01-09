package com.github.p3.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class BidInfoDto {
    private Long bidId; // 입찰 ID
    private String userNickname; // 사용자 닉네임
    private BigDecimal bidPrice; // 입찰 가격
    private LocalDateTime bidCreatedAt; // 입찰 시간
}
