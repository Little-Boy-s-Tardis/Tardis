package com.antigravity.chatprocessor.service;

import com.antigravity.chatprocessor.dto.ChatMessageDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class MessageConsumer {

    private final ChatBatcherService chatBatcherService;

    @RabbitListener(queues = "${app.rabbitmq.queue-name}")
    public void consumeMessage(ChatMessageDto message) {
        log.info("Consumed message from RabbitMQ. ID: {}, Sender: {}, Platform: {}, ConversationId: {}",
                message.getId(), message.getSender(), message.getPlatform(), message.getConversationId());
        
        try {
            chatBatcherService.addMessageToBatch(message);
        } catch (Exception e) {
            log.error("Failed to process consumed message ID: {}", message.getId(), e);
        }
    }
}
