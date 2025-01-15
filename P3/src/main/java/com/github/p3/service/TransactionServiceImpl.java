package com.github.p3.service;

import com.github.p3.entity.Product;
import com.github.p3.entity.ProductStatus;
import com.github.p3.entity.Transaction;
import com.github.p3.entity.TransactionStatus;
import com.github.p3.exception.CustomException;
import com.github.p3.exception.ErrorCode;
import com.github.p3.repository.ProductRepository;
import com.github.p3.repository.TransactionRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Transactional
public class TransactionServiceImpl implements TransactionsService {

    private final TransactionRepository transactionRepository;
    private final ProductRepository productRepository;

    @Override
    public void completeTransaction(Long transactionId) {
        // Transaction 가져오기
        Transaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new CustomException(ErrorCode.TRANSACTION_NOT_FOUND));

        // Transaction 상태 업데이트
        transaction.setStatus(TransactionStatus.거래완료);
        transactionRepository.save(transaction);

        // 연관된 Product 가져오기
        Product product = transaction.getProduct();
        if (product == null) {
            throw new CustomException(ErrorCode.PRODUCT_NOT_FOUND);
        }

        // Product 상태 업데이트
        product.setProductStatus(ProductStatus.거래완료);
        productRepository.save(product);
    }
}