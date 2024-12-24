package com.github.p3.repository;

import com.github.p3.entity.Bid;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BidRepository extends JpaRepository<Bid, Long> {
    Optional<Bid> findTopByProductProductIdOrderByBidCreatedAtDesc(Long productId);
}
