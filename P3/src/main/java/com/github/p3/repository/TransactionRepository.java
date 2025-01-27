package com.github.p3.repository;

import com.github.p3.entity.ProductStatus;
import com.github.p3.entity.Bid;
import com.github.p3.entity.Transaction;
import com.github.p3.entity.TransactionStatus;
import com.github.p3.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    // 거래 아이디로 거래를 찾을 때 사용
    Optional<Transaction> findByProductProductId(Long transactionId);

    // 상품 ID와 거래 상태에 따라 거래를 찾는 메서드
    Optional<Transaction> findByProduct_ProductIdAndStatus(Long productId, TransactionStatus status);

    List<Transaction> findByStatus(TransactionStatus transactionStatus);

    // productId를 기준으로 거래 정보를 조회
    Optional<Transaction> findByProduct_ProductId(Long productId);

    List<Transaction> findByBuyer_UserIdOrSeller_UserId(Integer buyer_userId, Integer seller_userId);
}