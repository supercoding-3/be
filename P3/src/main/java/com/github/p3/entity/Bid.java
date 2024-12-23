package com.github.p3.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@Table(name = "bids")
public class Bid {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "bid_id")
    private Long bidId; // 입찰 ID

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product; // 입찰한 상품 (Product 엔티티와 관계)

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user; // 입찰한 사용자 (User 엔티티와 관계)

    @Column(name = "bid_price", precision = 10, scale = 2)
    private BigDecimal bidPrice; // 입찰 가격

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BidStatus bidStatus = BidStatus.입찰; // 입찰 상태 (기본값: 입찰)

    @Column(name = "bid_created_at")
    private LocalDateTime bidCreatedAt = LocalDateTime.now(); // 입찰 시간

    @Column(name = "bid_updated_at")
    private LocalDateTime bidUpdatedAt = LocalDateTime.now(); // 입찰 업데이트 시간

    @Column(name = "bid_canceled_at")
    private LocalDateTime bidCanceledAt; // 입찰 취소 시간 (입찰 취소 시 기록)
}