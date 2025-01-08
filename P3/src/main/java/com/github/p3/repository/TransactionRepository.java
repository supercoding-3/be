package com.github.p3.repository;

import com.github.p3.entity.Transaction;
import com.github.p3.entity.TransactionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    // 거래 아이디로 거래를 찾을 때 사용
    Optional<Transaction> findByProduct_ProductId(Long transactionId);

    // 상품 ID와 거래 상태에 따라 거래를 찾는 메서드
    Optional<Transaction> findByProduct_ProductIdAndStatus(Long productId, TransactionStatus status);


}