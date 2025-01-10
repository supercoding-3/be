package com.github.p3.service;

import com.github.p3.entity.Transaction;
import com.github.p3.entity.TransactionStatus;
import com.github.p3.exception.CustomException;
import com.github.p3.exception.ErrorCode;
import com.github.p3.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TransactionServiceImpl implements TransactionsService{

    private final TransactionRepository transactionRepository;

    @Override
    public void completeTransaction(Long transactionId) {
        Transaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new CustomException(ErrorCode.TRANSACTION_NOT_FOUND));
        transaction.setStatus(TransactionStatus.거래완료);
        transactionRepository.save(transaction);
    }
}
