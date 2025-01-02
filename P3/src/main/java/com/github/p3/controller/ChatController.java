package com.github.p3.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.p3.dto.ChatMessageDto;
import com.github.p3.service.ChatMessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;

@Component
@RequiredArgsConstructor
@Slf4j
public class ChatController {

    private final ChatMessageService chatMessageService;

    @MessageMapping("/send")
    public void sendMessage(@Payload ChatMessageDto chatMessageDto, WebSocketSession session) {
        // 메시지 저장
        chatMessageService.saveMessage(chatMessageDto);

        // 채팅 메시지를 해당 사용자에게 전송 (WebSocket 세션을 사용)
        try {
            // 메시지 전송
            TextMessage message = new TextMessage(new ObjectMapper().writeValueAsString(chatMessageDto));
            session.sendMessage(message);
        } catch (IOException e) {
            log.error("Error sending message: {}", e.getMessage());
        }
    }
}
