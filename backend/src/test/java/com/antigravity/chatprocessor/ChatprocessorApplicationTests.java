package com.antigravity.chatprocessor;

import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import com.antigravity.chatprocessor.repository.AggregatedSummaryRepository;
import com.antigravity.chatprocessor.repository.RawWebhookMessageRepository;
import org.springframework.messaging.simp.SimpMessagingTemplate;

@SpringBootTest(properties = {
    "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration," +
    "org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration," +
    "org.springframework.boot.autoconfigure.amqp.RabbitAutoConfiguration"
})
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
