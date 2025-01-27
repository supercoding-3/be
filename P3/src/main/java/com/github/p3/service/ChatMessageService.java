package com.github.p3.service;

import com.github.p3.dto.ChatMessageDto;
import com.github.p3.dto.ChatRoomListDto;
import jakarta.transaction.Transactional;
import org.springframework.security.core.Authentication;

import java.util.List;

public interface ChatMessageService {

    Long createChatRoomId(Long productId);

    void saveMessage(ChatMessageDto chatMessageDto);

//    List<Long> getActChatRoomIds();

    List<ChatRoomListDto> getChatRoomList(Authentication authentication);

    void deleteChat(Long chatId);

    List<ChatMessageDto> getChatMessages(Long transactionId);
}
