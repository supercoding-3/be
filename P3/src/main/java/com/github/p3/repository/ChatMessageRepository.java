package com.github.p3.repository;

import com.github.p3.entity.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    List<ChatMessage> findByTransaction_TransactionId(Long transactionId);

}
