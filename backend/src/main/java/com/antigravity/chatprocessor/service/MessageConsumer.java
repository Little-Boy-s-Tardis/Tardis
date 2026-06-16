package com.antigravity.chatprocessor.service;

import com.antigravity.chatprocessor.dto.ChatMessageDto;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

@Service
public class MessageConsumer {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(MessageConsumer.class);

    private final ChatBatcherService chatBatcherService;

    public MessageConsumer(ChatBatcherService chatBatcherService) {
        this.chatBatcherService = chatBatcherService;
    }

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
