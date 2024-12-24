package com.github.p3.dto;

import com.github.p3.entity.Category;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class ProductDetailDto {
    private String title;
    private String description;
    private BigDecimal startingBidPrice;
    private BigDecimal immediatePrice;
    private Category category;
    private List<String> existingImageUrls; // 기존 이미지 URL
    private LocalDateTime productEndDate;
}
