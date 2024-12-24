package com.github.p3.dto;

import com.github.p3.entity.BidStatus;
import com.github.p3.entity.ProductStatus;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
public class CategoryDto {
    private String img;    // 첫 번째 이미지 URL
    private String title;  // 상품 제목
    private BigDecimal price;  // 즉시 구매 가격
    private String category;   // 카테고리 이름 (String으로 변환된 Category)
    private ProductStatus productStatus; // 상품 상태
}
