package com.antigravity.chatprocessor.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessageDto implements Serializable {
    private static final long serialVersionUID = 1L;

    private String id;
    private String sender;
    private String content;
    private String platform; // DISCORD, WHATSAPP
    private String conversationId;
    private Instant timestamp;
}
