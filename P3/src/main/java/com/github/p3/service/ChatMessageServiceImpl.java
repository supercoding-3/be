package com.github.p3.service;

import com.github.p3.dto.ChatMessageDto;
import com.github.p3.entity.ChatMessage;
import com.github.p3.entity.User;
import com.github.p3.mapper.ChatMessageMapper;
import com.github.p3.repository.ChatMessageRepository;
import com.github.p3.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ChatMessageServiceImpl implements ChatMessageService {

    private final ChatMessageMapper chatMessageMapper;
    private final ChatMessageRepository chatMessageRepository;
    private final UserRepository userRepository;

    public void saveMessage(ChatMessageDto chatMessageDto) {
        User sender = userRepository.findByUserEmail(chatMessageDto.getSender())
                .orElseThrow(() -> new RuntimeException("발신자를 찾을 수 없습니다."));
        User receiver = userRepository.findByUserEmail(chatMessageDto.getReceiver())
                .orElseThrow(() -> new RuntimeException("수신자를 찾을 수 없습니다."));

        ChatMessage chatMessage = chatMessageMapper.toChatMessageEntity(chatMessageDto);

        chatMessage.setSender(sender);
        chatMessage.setReceiver(receiver);

        chatMessageRepository.save(chatMessage);
    }
}
