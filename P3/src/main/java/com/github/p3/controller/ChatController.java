package com.github.p3.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.p3.dto.ChatMessageDto;
import com.github.p3.service.ChatMessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
@Slf4j
public class ChatController {

    private final ChatMessageService chatMessageService;
    private final ObjectMapper objectMapper;

    // WebSocket에서 메시지를 보내는 부분
    @MessageMapping("/send")
    public void sendMessage(@Payload ChatMessageDto chatMessageDto, WebSocketSession session) {
        // 메시지 저장
        chatMessageService.saveMessage(chatMessageDto);

        // 채팅 메시지를 해당 사용자에게 전송
        try {
            // 메시지 전송
            TextMessage message = new TextMessage(objectMapper.writeValueAsString(chatMessageDto));
            session.sendMessage(message);
        } catch (IOException e) {
            log.error("Error sending message: {}", e.getMessage());
        }
    }

    @DeleteMapping("/{chatId}")
    public ResponseEntity<String> deleteChat(@PathVariable Long chatId) {
        chatMessageService.deleteChat(chatId);
        log.info("{} 채팅 삭제 완료", chatId);
        return ResponseEntity.ok("채팅 메시지가 삭제되었습니다.");  // 204 No Content 응답
    }
}