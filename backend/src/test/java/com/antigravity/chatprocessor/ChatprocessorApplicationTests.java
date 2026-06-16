package com.antigravity.chatprocessor;

import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import com.antigravity.chatprocessor.repository.AggregatedSummaryRepository;
import com.antigravity.chatprocessor.repository.RawWebhookMessageRepository;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("dev")
class ChatprocessorApplicationTests {

    @MockitoBean
    private RabbitTemplate rabbitTemplate;

    @MockitoBean
    private AggregatedSummaryRepository aggregatedSummaryRepository;

    @MockitoBean
    private RawWebhookMessageRepository rawWebhookMessageRepository;

    @MockitoBean
    private SimpMessagingTemplate simpMessagingTemplate;

    @Test
    void contextLoads() {
    }

}
