package com.github.p3.service;

import com.github.p3.dto.ChatMessageDto;
import com.github.p3.entity.*;
import com.github.p3.exception.CustomException;
import com.github.p3.exception.ErrorCode;
import com.github.p3.mapper.ChatMessageMapper;
import com.github.p3.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ChatMessageServiceImpl implements ChatMessageService {

    private final ChatMessageMapper chatMessageMapper;
    private final ChatMessageRepository chatMessageRepository;
    private final UserRepository userRepository;
    private final TransactionRepository transactionRepository;

    public void saveMessage(ChatMessageDto chatMessageDto) {
        // 발신자와 수신자 조회
        User sender = userRepository.findByUserEmail(chatMessageDto.getSender())
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        User receiver = userRepository.findByUserEmail(chatMessageDto.getReceiver())
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        // 거래 중인 상품에 대한 거래 찾기
        Transaction transaction = transactionRepository.findByProduct_ProductIdAndStatus(
                        chatMessageDto.getProductId(), TransactionStatus.거래중)
                .orElseThrow(() -> new CustomException(ErrorCode.TRANSACTION_NOT_FOUND));

        // 발신자와 수신자가 거래에 참여한 사용자여야 함
        if (!(sender.getUserId().equals(transaction.getBuyer().getUserId()) || sender.getUserId().equals(transaction.getSeller().getUserId())) ||
                !(receiver.getUserId().equals(transaction.getBuyer().getUserId()) || receiver.getUserId().equals(transaction.getSeller().getUserId()))) {
            throw new CustomException(ErrorCode.UNAUTHORIZED_ACCESS);
        }

        // 상품 조회
        Product product = transaction.getProduct();

        // 채팅 메시지 생성 및 저장
        ChatMessage chatMessage = chatMessageMapper.toChatMessageEntity(chatMessageDto);
        chatMessage.setSender(sender);
        chatMessage.setReceiver(receiver);
        chatMessage.setProduct(product);

        chatMessageRepository.save(chatMessage);
    }
}