package com.github.p3.dto;

import com.github.p3.entity.Category;
import com.github.p3.entity.Product;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProductResponseDto {

    private Long productId;
    private String title;
    private String description;
    private BigDecimal startingBidPrice;
    private BigDecimal immediatePrice;
    private String category;
    private Integer userId;
    private List<String> imageUrls;

}