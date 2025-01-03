package com.github.p3.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.p3.dto.ChatMessageDto;
import com.github.p3.entity.ChatMessage;
import com.github.p3.entity.User;
import com.github.p3.service.ChatMessageServiceImpl;
import com.github.p3.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
@RequiredArgsConstructor
public class ChatWebSocketHandler extends TextWebSocketHandler {

    // 사용자 세션 관리
    private final ConcurrentHashMap<String, WebSocketSession> sessions = new ConcurrentHashMap<>();
    private final ObjectMapper objectMapper = new ObjectMapper(); // JSON 직렬화/역직렬화
    private final ChatMessageServiceImpl chatMessageServiceImpl;
    private final UserRepository userRepository;

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        String userEmail = (String) session.getAttributes().get("userEmail");
        if (userEmail != null) {
            sessions.put(userEmail, session);
            log.info("사용자 '{}' 연결", userEmail);
        } else {
            log.error("이메일을 세션에서 가져올 수 없습니다. 연결을 종료합니다.");
            session.close();
        }
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String payload = message.getPayload();
        log.info("메시지 내용: {}", payload);

        // JSON 메시지 역직렬화
        ChatMessage chatMessage = objectMapper.readValue(payload, ChatMessage.class);

        if (chatMessage.getSender() == null || chatMessage.getReceiver() == null) {
            log.error("송신자 또는 수신자 정보가 누락되었습니다. 송신자: {}, 수신자: {}",
                    chatMessage.getSender(), chatMessage.getReceiver());
            return; // 메시지 처리 종료
        }

        // 이메일을 기반으로 사용자를 조회
        Optional<User> senderUserOptional = userRepository.findByUserEmail(chatMessage.getSender().getUserEmail());
        if (senderUserOptional.isEmpty()) {
            log.error("송신자 이메일 {}에 해당하는 사용자 정보를 찾을 수 없습니다.", chatMessage.getSender().getUserEmail());
            return; // 사용자 정보가 없으면 처리 종료
        }

        Optional<User> receiverUserOptional = userRepository.findByUserEmail(chatMessage.getReceiver().getUserEmail());
        if (receiverUserOptional.isEmpty()) {
            log.error("수신자 이메일 {}에 해당하는 사용자 정보를 찾을 수 없습니다.", chatMessage.getReceiver().getUserEmail());
            return; // 사용자 정보가 없으면 처리 종료
        }

        // 사용자의 이메일 정보 로깅
        User senderUser = senderUserOptional.get();
        User receiverUser = receiverUserOptional.get();
        log.info("송신자 정보: {}", senderUser);
        log.info("수신자 정보: {}", receiverUser);

        // 채팅 메시지 저장
        ChatMessageDto chatMessageDto = ChatMessageDto.builder()
                .sender(senderUser.getUserEmail())
                .receiver(receiverUser.getUserEmail())
                .message(chatMessage.getMessage())
                .messageType(chatMessage.getMessageType())
                .productId(chatMessage.getProduct())
                .build();

        try {
            chatMessageServiceImpl.saveMessage(chatMessageDto);
        } catch (Exception e) {
            log.error("채팅 메시지 저장 중 오류 발생: {}", e.getMessage());
            return;
        }

        // 메시지 전달: 수신자 세션을 찾아 메시지 전송
        WebSocketSession receiverSession = sessions.get(receiverUser.getUserEmail());
        if (receiverSession != null && receiverSession.isOpen()) {
            receiverSession.sendMessage(new TextMessage(payload));
            log.info("메시지 전송: 사용자 '{}' -> 사용자 '{}'", chatMessage.getSender().getUserEmail(), chatMessage.getReceiver().getUserEmail());
        } else {
            log.error("수신자 '{}'의 세션이 열려 있지 않거나 존재하지 않습니다.", chatMessage.getReceiver().getUserEmail());
        }

        // 송신자에게 응답 메시지 전송
        WebSocketSession senderSession = sessions.get(senderUser.getUserEmail());
        if (senderSession != null && senderSession.isOpen()) {
            senderSession.sendMessage(new TextMessage(payload));
            log.info("송신자 '{}'에게 메시지 전송 완료", chatMessage.getSender().getUserEmail());
        } else {
            log.error("송신자 '{}'의 세션이 열려 있지 않거나 존재하지 않습니다.", chatMessage.getSender().getUserEmail());
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, org.springframework.web.socket.CloseStatus status) throws Exception {
        String userEmail = (String) session.getAttributes().get("userEmail");
        if (userEmail != null) {
            sessions.remove(userEmail);
            log.info("사용자 '{}' 연결 종료", userEmail);
        }
    }
}