package com.github.p3.mapper;

import com.github.p3.dto.ChatMessageDto;
import com.github.p3.entity.ChatMessage;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ChatMessageMapper {

    @Mapping(source = "sender", target = "sender.userEmail")
    @Mapping(source = "receiver", target = "receiver.userEmail")
    @Mapping(source = "productId", target = "product.productId")
    ChatMessage toChatMessageEntity(ChatMessageDto chatMessageDto);

    @Mapping(source = "sender.userEmail", target = "sender")
    @Mapping(source = "receiver.userEmail", target = "receiver")
    @Mapping(source = "product.productId", target = "productId")
    ChatMessageDto toChatMessageDto(ChatMessage chatMessage);
}