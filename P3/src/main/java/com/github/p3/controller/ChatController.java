package com.github.p3.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.p3.dto.ChatMessageDto;
import com.github.p3.dto.ChatRoomListDto;
import com.github.p3.service.ChatMessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
@Slf4j
public class ChatController {

    private final ChatMessageService chatMessageService;
    private final ObjectMapper objectMapper;

    // 채팅방 생성
    @PostMapping("/room")
    public ResponseEntity<String> createChatRoom(@RequestBody ChatMessageDto chatMessageDto) {
        Long chatRoomId = chatMessageService.createChatRoomId(chatMessageDto.getProductId());
        return ResponseEntity.ok("채팅방 생성 : " + chatRoomId);
    }

    // WebSocket 메시지 전송
    @MessageMapping("/room/{chatRoomId}")
    public void sendMessage(@PathVariable Long chatRoomId, @Payload ChatMessageDto chatMessageDto, WebSocketSession session) {
        chatMessageDto.setChatRoomId(chatRoomId);

        chatMessageService.saveMessage(chatMessageDto); // 메시지 저장

        try {
            TextMessage message = new TextMessage(objectMapper.writeValueAsString(chatMessageDto));
            session.sendMessage(message);
        } catch (IOException e) {
            log.error("전송 메시지 에러: {}", e.getMessage());
        }
    }

    // 채팅 내용 조회
    @GetMapping("/room/{chatRoomId}")
    public ResponseEntity<List<ChatMessageDto>> getChatMessages(@PathVariable("chatRoomId") Long chatRoomId) {
        List<ChatMessageDto> chatMessages = chatMessageService.getChatMessages(chatRoomId);
        return ResponseEntity.ok(chatMessages);
    }

    // 채팅방 목록 조회
    @GetMapping("/rooms")
    public ResponseEntity<List<ChatRoomListDto>> getChatRooms(Authentication authentication) {
        List<ChatRoomListDto> chatRoomList = chatMessageService.getChatRoomList(authentication);
        return ResponseEntity.ok(chatRoomList);
    }

    // 채팅방 나가기(삭제)
    @DeleteMapping("/{chatId}")
    public ResponseEntity<String> deleteChat(@PathVariable Long chatId) {
        chatMessageService.deleteChat(chatId);
        log.info("{} 채팅 삭제 완료", chatId);
        return ResponseEntity.ok("채팅 메시지가 삭제되었습니다.");
    }
}