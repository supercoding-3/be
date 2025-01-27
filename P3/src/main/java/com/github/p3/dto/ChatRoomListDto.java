package com.github.p3.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatRoomListDto {

    private Long chatRoomId;
    private String productName;
    private BigDecimal productPrice;
    private String productProfileImageUrl;
    private String oppositeNickname;   // 상대방 닉네임
    private String oppositeProfileImageUrl; // 상대방 프로필 이미지 URL
}
