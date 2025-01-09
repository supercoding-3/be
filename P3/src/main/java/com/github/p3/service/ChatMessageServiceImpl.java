package com.github.p3.service;

import com.github.p3.dto.ChatMessageDto;
import com.github.p3.entity.*;
import com.github.p3.exception.CustomException;
import com.github.p3.exception.ErrorCode;
import com.github.p3.mapper.ChatMessageMapper;
import com.github.p3.repository.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChatMessageServiceImpl implements ChatMessageService {

    private final ChatMessageMapper chatMessageMapper;
    private final ChatMessageRepository chatMessageRepository;
    private final UserRepository userRepository;
    private final TransactionRepository transactionRepository;

    // 채팅방 번호 조회
    @Override
    @Transactional
    public Long createChatRoomId(Long productId) {
        // 거래 중인 상품 조회
        Product product = transactionRepository.findByProductProductId(productId)
                .orElseThrow(() -> new CustomException(ErrorCode.PRODUCT_NOT_FOUND))
                .getProduct();

        // 거래 중인 상품에 대한 거래 정보 조회
        Transaction transaction = transactionRepository.findByProduct_ProductIdAndStatus(
                        product.getProductId(), TransactionStatus.거래중)
                .orElseThrow(() -> new CustomException(ErrorCode.TRANSACTION_NOT_FOUND));

        // 거래 정보에서 구매자와 판매자 조회
        User buyer = userRepository.findByUserId(transaction.getBuyer().getUserId())
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND)); // 구매자 조회
        User seller = userRepository.findByUserId(transaction.getSeller().getUserId())
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND)); // 판매자 조회

        // 채팅방 번호 반환 (거래 ID)
        return transaction.getTransactionId();
    }

    @Override
    @Transactional
    public void saveMessage(ChatMessageDto chatMessageDto) {
        // 이미 전달된 거래 ID로 거래 정보 조회
        Transaction transaction = transactionRepository.findById(chatMessageDto.getTransactionId())
                .orElseThrow(() -> new CustomException(ErrorCode.TRANSACTION_NOT_FOUND));

        User sender = userRepository.findByUserEmail(chatMessageDto.getSender())
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        User receiver = userRepository.findByUserEmail(chatMessageDto.getReceiver())
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        Product product = transaction.getProduct();
        if (product == null) {
            throw new CustomException(ErrorCode.PRODUCT_NOT_FOUND);
        }

        // 채팅 메시지 생성
        ChatMessage chatMessage = chatMessageMapper.toChatMessageEntity(chatMessageDto);
        chatMessage.setTransaction(transaction); // 거래 정보 설정
        chatMessage.setSender(sender);
        chatMessage.setReceiver(receiver);
        chatMessage.setProduct(product);

        // 채팅 메시지 저장
        chatMessageRepository.save(chatMessage);
    }

    // 채팅 내용 조회
    @Override
    @Transactional
    public List<ChatMessageDto> getChatMessages(Long transactionId) {
        List<ChatMessage> chatMessages = chatMessageRepository.findByTransaction_TransactionId(transactionId);

        return chatMessages.stream()
                .map(chatMessageMapper::toChatMessageDto)
                .collect(Collectors.toList());
    }

    // 채팅 목록 조회
    @Override
    @Transactional
    public List<Long> getActChatRoomIds() {
        List<Transaction> activeTransactions = transactionRepository.findByStatus(TransactionStatus.거래중);

        return activeTransactions.stream()
                .map(Transaction::getTransactionId)
                .collect(Collectors.toList());
    }


    // 채팅방 나가기(삭제)
    @Override
    public void deleteChat(Long chatId) {
        ChatMessage chatMessage = chatMessageRepository.findById(chatId)
                .orElseThrow(() -> new CustomException(ErrorCode.CHAT_NOT_FOUND));
        chatMessageRepository.delete(chatMessage);
    }
}