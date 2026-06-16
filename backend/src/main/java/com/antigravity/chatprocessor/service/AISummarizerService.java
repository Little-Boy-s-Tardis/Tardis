package com.antigravity.chatprocessor.service;

import com.antigravity.chatprocessor.dto.ChatMessageDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class AISummarizerService {

    public void summarizeBatch(String conversationId, List<ChatMessageDto> messages) {
        log.info("AISummarizerService received batch for conversationId: {} with {} messages", 
                conversationId, messages.size());
        
        for (ChatMessageDto msg : messages) {
            log.debug("Message in batch: [{}] {}: {}", msg.getPlatform(), msg.getSender(), msg.getContent());
        }

        // TODO: Call LLM API (OpenAI/Gemini/Claude)
        // TODO: Save messages and summary to PostgreSQL database
        // TODO: Push to WebSocket dashboard
        log.info("Successfully processed and stubbed batch summarization for conversationId: {}", conversationId);
    }
}
