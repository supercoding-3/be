package com.github.p3.repository;

import com.github.p3.entity.Bid;
import com.github.p3.entity.Transaction;
import com.github.p3.entity.TransactionStatus;
import com.github.p3.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    // productId를 기준으로 거래 정보를 조회
    Optional<Transaction> findByProduct_ProductId(Long productId);

}
