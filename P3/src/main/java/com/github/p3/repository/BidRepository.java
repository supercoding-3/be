package com.github.p3.repository;

import com.github.p3.entity.Bid;
import com.github.p3.entity.BidStatus;
import com.github.p3.entity.Product;
import com.github.p3.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BidRepository extends JpaRepository<Bid, Long> {
    Optional<Bid> findTopByProductProductIdOrderByBidCreatedAtDesc(Long productId);

    List<Bid> findByProductProductIdOrderByBidCreatedAtDesc(Long productId);

    Optional<Bid> findByProductAndBidStatusAndUser(Product product, BidStatus bidStatus, User sender);

    List<Bid> findByUser_UserId(Integer userId);  // User 엔티티의 userId를 기준으로 조회
}
