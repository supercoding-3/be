package com.github.p3.dto;

import com.github.p3.entity.Bid;
import com.github.p3.entity.Category;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Data
@AllArgsConstructor
public class ProductDetailResponseDto {

    private Long productId;
    private String title;
    private String description;
    private BigDecimal startingBidPrice;
    private BigDecimal immediatePrice;
    private Category category; // 카테고리 필드
    private List<String> imageUrls;
    private Bid latestBid;
    private Boolean isSeller; // 판매자인지 여부

}