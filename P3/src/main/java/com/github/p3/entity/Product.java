package com.github.p3.entity;


import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
@Table(name = "products")
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "product_id")
    private Long productId;

    @Enumerated(EnumType.STRING)
    private Category category;

    @Column(nullable = false, length = 255)
    private String title; // 상품명

    @Column(columnDefinition = "TEXT")
    private String description; // 상품 설명

    @Column(name = "starting_bid_price", precision = 10, scale = 2)
    private BigDecimal startingBidPrice; // 경매 시작가

    @Column(name = "immediate_price", precision = 10, scale = 2)
    private BigDecimal immediatePrice; // 즉시 구매가

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProductStatus productStatus = ProductStatus.입찰중; // 상품 상태 기본값

    @Column(name = "product_created_at", updatable = false)
    private LocalDateTime productCreatedAt = LocalDateTime.now();

    @Column(name = "product_updated_at")
    private LocalDateTime productUpdatedAt = LocalDateTime.now();

    @Column(name = "product_end_date")
    private LocalDateTime productEndDate;

    // 외래 키 설정
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false) // 외래 키 설정
    private User user; // 사용자 엔티티와의 관계

    // 이미지 리스트 (Image 엔티티와의 관계)
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Image> images = new ArrayList<>(); // 여러 이미지를 관리할 수 있는 필드

    // 입찰 리스트 (Bid 엔티티와의 관계)
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Bid> bids = new ArrayList<>(); // 상품에 대한 모든 입찰
}

