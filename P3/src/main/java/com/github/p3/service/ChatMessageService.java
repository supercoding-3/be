package com.github.p3.service;

import com.github.p3.dto.ChatMessageDto;

import java.util.List;

public interface ChatMessageService {

    Long createChatRoomId(Long productId, String buyerEmail, String sellerEmail);

    void saveMessage(ChatMessageDto chatMessageDto);

    void deleteChat(Long chatId);

    List<ChatMessageDto> getChatMessages(Long transactionId);
}
