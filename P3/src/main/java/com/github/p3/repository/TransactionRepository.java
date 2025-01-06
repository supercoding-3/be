package com.github.p3.repository;

import com.github.p3.entity.Transaction;
import com.github.p3.entity.TransactionStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    Optional<Transaction> findByProduct_ProductIdAndStatus(Long productId, TransactionStatus status);
}
