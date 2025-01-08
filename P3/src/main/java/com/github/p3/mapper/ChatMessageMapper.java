package com.github.p3.mapper;

import com.github.p3.dto.ChatMessageDto;
import com.github.p3.entity.ChatMessage;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ChatMessageMapper {

    @Mapping(target = "sender.userEmail", source = "sender") // 이메일을 통해 사용자 조회
    @Mapping(target = "receiver.userEmail", source = "receiver")
    @Mapping(target = "transaction.transactionId", source = "transactionId") // 거래 ID로 설정
    @Mapping(target = "product.productId", source = "productId") // 상품 ID로 설정
    ChatMessage toChatMessageEntity(ChatMessageDto chatMessageDto);

    @Mapping(target = "transactionId", source = "transaction.transactionId")
    @Mapping(target = "sender", source = "sender.userEmail")
    @Mapping(target = "receiver", source = "receiver.userEmail")
    @Mapping(target = "productId", source = "product.productId")
    ChatMessageDto toChatMessageDto(ChatMessage chatMessage);
}