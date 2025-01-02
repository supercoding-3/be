package com.github.p3.dto;

import com.github.p3.entity.MessageType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatMessageDto {

    private Long chatId;
    private String sender;
    private String receiver;
    private String message;
    private MessageType messageType;

}
