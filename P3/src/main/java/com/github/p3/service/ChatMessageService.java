package com.github.p3.service;

import com.github.p3.dto.ChatMessageDto;
import jakarta.transaction.Transactional;

import java.util.List;

public interface ChatMessageService {

    Long createChatRoomId(Long productId);

    void saveMessage(ChatMessageDto chatMessageDto);

    List<Long> getActChatRoomIds();

    void deleteChat(Long chatId);

    List<ChatMessageDto> getChatMessages(Long transactionId);
}
