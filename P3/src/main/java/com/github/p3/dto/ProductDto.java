package com.github.p3.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class ProductDto {
    private Long productId;       // 상품 ID
    private String title;   // 상품명
    private String img;           // 상품 이미지 URL
    private BigDecimal price;           // 가격
    private String productStatus; // 상품 상태
}
