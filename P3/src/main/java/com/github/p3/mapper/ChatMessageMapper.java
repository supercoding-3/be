package com.github.p3.mapper;

import com.github.p3.dto.ChatMessageDto;
import com.github.p3.entity.ChatMessage;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ChatMessageMapper {

    @Mapping(target = "sender.userEmail", source = "sender")
    @Mapping(target = "receiver.userEmail", source = "receiver")
    @Mapping(target = "transaction.transactionId", source = "chatRoomId") // 여기를 수정!
    @Mapping(target = "product.productId", source = "productId")
    ChatMessage toChatMessageEntity(ChatMessageDto chatMessageDto);

    @Mapping(target = "chatRoomId", source = "transaction.transactionId") // 여기도 통일!
    @Mapping(target = "sender", source = "sender.userEmail")
    @Mapping(target = "receiver", source = "receiver.userEmail")
    @Mapping(target = "productId", source = "product.productId")
    ChatMessageDto toChatMessageDto(ChatMessage chatMessage);
}