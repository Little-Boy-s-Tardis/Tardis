package com.antigravity.chatprocessor.config;

import com.antigravity.chatprocessor.dto.ChatMessageDto;
import com.antigravity.chatprocessor.service.MessageConsumer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Profile;

import java.lang.reflect.Proxy;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Profile("dev")
@Configuration
public class DevMessagingConfig {

    private final MessageConsumer messageConsumer;

    public DevMessagingConfig(@Lazy MessageConsumer messageConsumer) {
        this.messageConsumer = messageConsumer;
    }

    @Bean
    public RabbitTemplate rabbitTemplate() {
        log.info("Creating in-memory simulated RabbitTemplate for 'dev' profile.");
        
        // Create a native Java dynamic proxy to mock ConnectionFactory without Mockito test dependency
        ConnectionFactory simulatedConnectionFactory = (ConnectionFactory) Proxy.newProxyInstance(
                ConnectionFactory.class.getClassLoader(),
                new Class<?>[]{ConnectionFactory.class},
                (proxy, method, args) -> {
                    if ("toString".equals(method.getName())) {
                        return "SimulatedConnectionFactory";
                    }
                    return null;
                }
        );
        
        return new RabbitTemplate(simulatedConnectionFactory) {
            @Override
            public void convertAndSend(String exchange, String routingKey, Object message) throws AmqpException {
                log.info("[SIMULATED QUEUE] Routing message via exchange: {}, routingKey: {}", exchange, routingKey);
                if (message instanceof ChatMessageDto chatMessage) {
                    CompletableFuture.runAsync(() -> {
                        try {
                            // Simulate 50ms queue network delay
                            Thread.sleep(50);
                            messageConsumer.consumeMessage(chatMessage);
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                        }
                    });
                } else {
                    log.warn("Simulated queue received unknown message type: {}", message.getClass());
                }
            }
        };
    }
}
