package com.github.p3.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
public class MyPageResponseDto {
    private List<ProductDto> bidProducts;
    private List<ProductDto> soldProducts;
    private String nickname;  // 유저의 닉네임 추가

    // 생성자 추가
    public MyPageResponseDto(List<ProductDto> bidProducts, List<ProductDto> soldProducts, String nickname) {
        this.bidProducts = bidProducts;
        this.soldProducts = soldProducts;
        this.nickname = nickname;
    }
}
