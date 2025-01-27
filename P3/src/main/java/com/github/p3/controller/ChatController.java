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

    // 채팅방 번호 반환
    @PostMapping("/room")
    public ResponseEntity<String> createChatRoom(@RequestBody ChatMessageDto chatMessageDto) {
        Long transactionId = chatMessageService.createChatRoomId(chatMessageDto.getProductId());
        return ResponseEntity.ok("채팅방 생성 : " + transactionId);
    }

    // WebSocket에서 메시지를 보내는 부분
    @MessageMapping("/room/{transactionId}")
    public void sendMessage(@PathVariable Long transactionId, @Payload ChatMessageDto chatMessageDto, WebSocketSession session) {
        chatMessageDto.setTransactionId(transactionId);

        // 메시지 저장
        chatMessageService.saveMessage(chatMessageDto);

        // 채팅 메시지를 해당 사용자에게 전송
        try {
            // 메시지 전송
            TextMessage message = new TextMessage(objectMapper.writeValueAsString(chatMessageDto));
            session.sendMessage(message);
        } catch (IOException e) {
            log.error("전송 메시지 에러: {}", e.getMessage());
        }
    }

    // 채팅 내용 조회
    @GetMapping("/room/{transactionId}")
    public ResponseEntity<List<ChatMessageDto>> getChatMessages(@PathVariable Long transactionId) {
        List<ChatMessageDto> chatMessages = chatMessageService.getChatMessages(transactionId);
        return ResponseEntity.ok(chatMessages);
    }

    // 채팅 목록 조회
//    @GetMapping("/rooms")
//    public ResponseEntity<List<Long>> getChatRoomList() {
//        List<Long> chatRoomIds = chatMessageService.getActChatRoomIds();
//        return ResponseEntity.ok(chatRoomIds);
//    }

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
        return ResponseEntity.ok("채팅 메시지가 삭제되었습니다.");  // 204 No Content 응답
    }
}