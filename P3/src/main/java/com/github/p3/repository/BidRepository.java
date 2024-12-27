package com.github.p3.repository;

import com.github.p3.entity.Bid;
import com.github.p3.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BidRepository extends JpaRepository<Bid, Long> {
    Optional<Bid> findTopByProductProductIdOrderByBidCreatedAtDesc(Long productId);

    Optional<Bid> findTopByProductOrderByBidPriceDesc(Product product);

    List<Bid> findByProductProductIdOrderByBidCreatedAtDesc(Long productId);
}
