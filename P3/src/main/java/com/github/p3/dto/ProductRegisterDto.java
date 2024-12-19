package com.github.p3.dto;

import com.github.p3.entity.Category;
import lombok.*;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProductRegisterDto {

    private String title;
    private String description;
    private BigDecimal startingBidPrice;
    private BigDecimal immediatePrice;
    private Category category; // enum 타입
    private Integer userId;
    private List<MultipartFile> images; // 이미지를 MultipartFile로 받음
    private LocalDateTime productEndDate;

}
