package com.github.p3.controller;

import com.github.p3.service.TransactionsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionsService transactionsService;

    @PatchMapping("/transaction/{transactionId}")
    public ResponseEntity<String> completeTransaction(@PathVariable Long transactionId) {
        transactionsService.completeTransaction(transactionId);
        return ResponseEntity.ok("거래완료 되었습니다.");
    }
}
