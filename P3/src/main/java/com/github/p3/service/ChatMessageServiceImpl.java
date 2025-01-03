package com.github.p3.service;

import com.github.p3.dto.ChatMessageDto;
import com.github.p3.entity.*;
import com.github.p3.exception.CustomException;
import com.github.p3.exception.ErrorCode;
import com.github.p3.mapper.ChatMessageMapper;
import com.github.p3.repository.BidRepository;
import com.github.p3.repository.ChatMessageRepository;
import com.github.p3.repository.ProductRepository;
import com.github.p3.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ChatMessageServiceImpl implements ChatMessageService {

    private final ChatMessageMapper chatMessageMapper;
    private final ChatMessageRepository chatMessageRepository;
    private final UserRepository userRepository;
    private final BidRepository bidRepository;
    private final ProductRepository productRepository;

    public void saveMessage(ChatMessageDto chatMessageDto) {
        // 발신자와 수신자 조회
        User sender = userRepository.findByUserEmail(chatMessageDto.getSender())
                .orElseThrow(() -> new CustomException(ErrorCode.SENDER_NOT_FOUND));
        User receiver = userRepository.findByUserEmail(chatMessageDto.getReceiver())
                .orElseThrow(() -> new CustomException(ErrorCode.RECEIVER_NOT_FOUND));

        // 상품 정보 조회
        Product product = productRepository.findById(chatMessageDto.getProductId().getProductId())
                .orElseThrow(() -> new CustomException(ErrorCode.PRODUCT_NOT_FOUND));

        // 낙찰 상품 상태 확인
        Optional<Bid> award = bidRepository.findByProductAndBidStatusAndUser(product, BidStatus.낙찰, sender);

        if (award.isEmpty()) {
            throw new CustomException(ErrorCode.AWARD_NOT_FOUND);
        }

        // 채팅 메시지 생성 및 저장
        ChatMessage chatMessage = chatMessageMapper.toChatMessageEntity(chatMessageDto);
        chatMessage.setSender(sender);
        chatMessage.setReceiver(receiver);
        chatMessage.setProduct(product);

        chatMessageRepository.save(chatMessage);
    }
}
