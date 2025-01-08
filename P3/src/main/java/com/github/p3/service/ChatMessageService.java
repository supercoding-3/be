package com.github.p3.service;

import com.github.p3.dto.ChatMessageDto;

public interface ChatMessageService {

    Long createChatRoomId(Long productId, String buyerEmail, String sellerEmail);

    void saveMessage(ChatMessageDto chatMessageDto);

    void deleteChat(Long chatId);
}
