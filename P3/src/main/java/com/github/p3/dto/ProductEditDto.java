package com.github.p3.dto;

import com.github.p3.entity.Category;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
public class ProductEditDto {
    private String title;
    private String description;
    private BigDecimal startingBidPrice;
    private BigDecimal immediatePrice;
    private Category category; // enum 타입
    private List<String> existingImageUrls; // 기존 이미지 URL들
    private List<MultipartFile> images = new ArrayList<>(); // 새로 업로드된 이미지
    private LocalDateTime productEndDate; // 상품 종료일자

}
