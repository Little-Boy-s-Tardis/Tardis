package com.antigravity.chatprocessor.controller;

import com.antigravity.chatprocessor.dto.ChatMessageDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/v1/webhooks")
@RequiredArgsConstructor
public class WebhookController {

    private final RabbitTemplate rabbitTemplate;

    @Value("${app.webhook.verify-token}")
    private String verifyToken;

    @Value("${app.rabbitmq.exchange-name}")
    private String exchangeName;

    @Value("${app.rabbitmq.routing-key}")
    private String routingKey;

    /**
     * Exposes the verification endpoint required by WhatsApp to register webhooks.
     * When WhatsApp registers a webhook, it sends a GET request with a hub.challenge,
     * hub.mode ("subscribe"), and hub.verify_token. We verify the token and return the challenge.
     */
    @GetMapping("/whatsapp")
    public ResponseEntity<String> verifyWhatsappWebhook(
            @RequestParam(value = "hub.mode", required = false) String mode,
            @RequestParam(value = "hub.verify_token", required = false) String token,
            @RequestParam(value = "hub.challenge", required = false) String challenge) {
        
        log.info("Received WhatsApp verification request. Mode: {}, Token: {}", mode, token);

        if ("subscribe".equals(mode) && isValidToken(token)) {
            log.info("WhatsApp webhook verified successfully.");
            return ResponseEntity.ok(challenge);
        } else {
            log.warn("WhatsApp webhook verification failed. Invalid token: {}", token);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }

    @PostMapping("/discord")
    public ResponseEntity<Void> handleDiscordWebhook(
            @RequestHeader(value = "X-Webhook-Token", required = false) String token,
            @RequestBody ChatMessageDto messageDto) {
        
        log.info("Received Discord webhook message from sender: {}", messageDto.getSender());

        if (!isValidToken(token)) {
            log.warn("Unauthorized Discord webhook attempt with token: {}", token);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        // Enforce business rules / sanitize inputs
        if (messageDto.getId() == null) {
            messageDto.setId(UUID.randomUUID().toString());
        }
        if (messageDto.getTimestamp() == null) {
            messageDto.setTimestamp(Instant.now());
        }
        messageDto.setPlatform("DISCORD");

        // Publish to queue asynchronously
        rabbitTemplate.convertAndSend(exchangeName, routingKey, messageDto);
        log.debug("Successfully queued Discord message ID: {}", messageDto.getId());

        // Immediately return 200 OK
        return ResponseEntity.ok().build();
    }

    @PostMapping("/whatsapp")
    public ResponseEntity<Void> handleWhatsappWebhook(
            @RequestHeader(value = "X-Webhook-Token", required = false) String token,
            @RequestBody ChatMessageDto messageDto) {

        log.info("Received WhatsApp webhook message from sender: {}", messageDto.getSender());

        if (!isValidToken(token)) {
            log.warn("Unauthorized WhatsApp webhook attempt with token: {}", token);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        // Enforce business rules / sanitize inputs
        if (messageDto.getId() == null) {
            messageDto.setId(UUID.randomUUID().toString());
        }
        if (messageDto.getTimestamp() == null) {
            messageDto.setTimestamp(Instant.now());
        }
        messageDto.setPlatform("WHATSAPP");

        // Publish to queue asynchronously
        rabbitTemplate.convertAndSend(exchangeName, routingKey, messageDto);
        log.debug("Successfully queued WhatsApp message ID: {}", messageDto.getId());

        // Immediately return 200 OK
        return ResponseEntity.ok().build();
    }

    private boolean isValidToken(String token) {
        return verifyToken.equals(token);
    }
}
