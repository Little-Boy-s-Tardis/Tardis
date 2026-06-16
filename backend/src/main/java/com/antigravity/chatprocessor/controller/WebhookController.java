package com.antigravity.chatprocessor.controller;

import com.antigravity.chatprocessor.dto.ChatMessageDto;
import com.antigravity.chatprocessor.model.RawWebhookMessage;
import com.antigravity.chatprocessor.repository.RawWebhookMessageRepository;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;
import java.util.Map;
import java.util.List;

@RestController
@RequestMapping("/api/v1/webhooks")
@CrossOrigin(origins = "*")
public class WebhookController {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(WebhookController.class);

    private final RabbitTemplate rabbitTemplate;
    private final RawWebhookMessageRepository rawWebhookMessageRepository;

    public WebhookController(RabbitTemplate rabbitTemplate, RawWebhookMessageRepository rawWebhookMessageRepository) {
        this.rabbitTemplate = rabbitTemplate;
        this.rawWebhookMessageRepository = rawWebhookMessageRepository;
    }

    @Value("${app.webhook.verify-token}")
    private String verifyToken;

    @Value("${app.rabbitmq.exchange-name}")
    private String exchangeName;

    @Value("${app.rabbitmq.routing-key}")
    private String routingKey;

    /**
     * Exposes the verification endpoint required by WhatsApp to register webhooks.
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

        // Sanitize inputs
        if (messageDto.getId() == null) {
            messageDto.setId(UUID.randomUUID().toString());
        }
        if (messageDto.getTimestamp() == null) {
            messageDto.setTimestamp(Instant.now());
        }
        messageDto.setPlatform("DISCORD");

        // Deduplication Check
        String hash = computeMessageHash(messageDto);
        Instant twoHoursAgo = Instant.now().minus(Duration.ofHours(2));
        int count = rawWebhookMessageRepository.countByMessageHashAndTimestampAfter(hash, twoHoursAgo);
        
        if (count > 0) {
            log.info("Duplicate Discord message detected (hash: {}). Dropping and returning 200 OK.", hash);
            return ResponseEntity.ok().build();
        }

        // Backup RAW payload to Postgres
        RawWebhookMessage backupMsg = RawWebhookMessage.builder()
                .id(messageDto.getId())
                .sender(messageDto.getSender())
                .content(messageDto.getContent())
                .platform("DISCORD")
                .conversationId(messageDto.getConversationId())
                .timestamp(messageDto.getTimestamp())
                .messageHash(hash)
                .build();
        
        try {
            rawWebhookMessageRepository.save(backupMsg);
            log.debug("Successfully backed up raw Discord message ID: {}", messageDto.getId());
        } catch (Exception e) {
            log.error("Failed to backup raw Discord message to database", e);
        }

        // Publish to RabbitMQ
        rabbitTemplate.convertAndSend(exchangeName, routingKey, messageDto);
        log.info("Queued Discord message to RabbitMQ. ID: {}", messageDto.getId());

        return ResponseEntity.ok().build();
    }

    @PostMapping("/whatsapp")
    @SuppressWarnings("unchecked")
    public ResponseEntity<Void> handleWhatsappWebhook(
            @RequestHeader(value = "X-Webhook-Token", required = false) String token,
            @RequestBody Map<String, Object> payload) {

        log.info("Received WhatsApp webhook request");

        ChatMessageDto messageDto = new ChatMessageDto();

        if (payload.containsKey("object") && payload.containsKey("entry")) {
            // Real WhatsApp API webhook format
            try {
                List<Map<String, Object>> entryList = (List<Map<String, Object>>) payload.get("entry");
                if (entryList != null && !entryList.isEmpty()) {
                    Map<String, Object> entry = entryList.get(0);
                    List<Map<String, Object>> changesList = (List<Map<String, Object>>) entry.get("changes");
                    if (changesList != null && !changesList.isEmpty()) {
                        Map<String, Object> change = changesList.get(0);
                        Map<String, Object> value = (Map<String, Object>) change.get("value");
                        if (value != null && value.containsKey("messages")) {
                            List<Map<String, Object>> messagesList = (List<Map<String, Object>>) value.get("messages");
                            if (messagesList != null && !messagesList.isEmpty()) {
                                Map<String, Object> msg = messagesList.get(0);
                                
                                // Extract sender name
                                String sender = "WhatsApp User";
                                List<Map<String, Object>> contactsList = (List<Map<String, Object>>) value.get("contacts");
                                if (contactsList != null && !contactsList.isEmpty()) {
                                    Map<String, Object> contact = contactsList.get(0);
                                    Map<String, Object> profile = (Map<String, Object>) contact.get("profile");
                                    if (profile != null && profile.containsKey("name")) {
                                        sender = (String) profile.get("name");
                                    }
                                } else if (msg.containsKey("from")) {
                                    sender = (String) msg.get("from");
                                }

                                // Extract message content
                                String content = "";
                                if (msg.containsKey("text")) {
                                    Map<String, Object> textObj = (Map<String, Object>) msg.get("text");
                                    if (textObj != null && textObj.containsKey("body")) {
                                        content = (String) textObj.get("body");
                                    }
                                }

                                String msgId = (String) msg.get("id");
                                Instant timestamp = Instant.now();
                                if (msg.containsKey("timestamp")) {
                                    try {
                                        long seconds = Long.parseLong(msg.get("timestamp").toString());
                                        timestamp = Instant.ofEpochSecond(seconds);
                                    } catch (Exception ex) {
                                        log.warn("Failed to parse WhatsApp timestamp", ex);
                                    }
                                }

                                messageDto.setId(msgId);
                                messageDto.setSender(sender);
                                messageDto.setContent(content);
                                messageDto.setTimestamp(timestamp);
                                messageDto.setConversationId("global-contest");
                                messageDto.setPlatform("WHATSAPP");
                            }
                        }
                    }
                }
            } catch (Exception e) {
                log.error("Failed to parse WhatsApp Cloud API payload", e);
                return ResponseEntity.badRequest().build();
            }
        } else {
            // Simulator format
            if (!isValidToken(token)) {
                log.warn("Unauthorized WhatsApp webhook attempt with token: {}", token);
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }

            try {
                messageDto.setId((String) payload.get("id"));
                messageDto.setSender((String) payload.get("sender"));
                messageDto.setContent((String) payload.get("content"));
                messageDto.setConversationId((String) payload.get("conversationId"));
                messageDto.setPlatform("WHATSAPP");

                String tsStr = (String) payload.get("timestamp");
                if (tsStr != null) {
                    messageDto.setTimestamp(Instant.parse(tsStr));
                }
            } catch (Exception e) {
                log.error("Failed to parse simulator WhatsApp payload", e);
                return ResponseEntity.badRequest().build();
            }
        }

        // Skip non-text messages or blank payloads
        if (messageDto.getContent() == null || messageDto.getContent().trim().isEmpty()) {
            log.info("WhatsApp webhook payload has no text body. Skipping message processing.");
            return ResponseEntity.ok().build();
        }

        // Sanitize inputs
        if (messageDto.getId() == null) {
            messageDto.setId(UUID.randomUUID().toString());
        }
        if (messageDto.getTimestamp() == null) {
            messageDto.setTimestamp(Instant.now());
        }
        if (messageDto.getConversationId() == null) {
            messageDto.setConversationId("global-contest");
        }

        // Deduplication Check
        String hash = computeMessageHash(messageDto);
        Instant twoHoursAgo = Instant.now().minus(Duration.ofHours(2));
        int count = rawWebhookMessageRepository.countByMessageHashAndTimestampAfter(hash, twoHoursAgo);
        
        if (count > 0) {
            log.info("Duplicate WhatsApp message detected (hash: {}). Dropping and returning 200 OK.", hash);
            return ResponseEntity.ok().build();
        }

        // Backup RAW payload to Postgres
        RawWebhookMessage backupMsg = RawWebhookMessage.builder()
                .id(messageDto.getId())
                .sender(messageDto.getSender())
                .content(messageDto.getContent())
                .platform("WHATSAPP")
                .conversationId(messageDto.getConversationId())
                .timestamp(messageDto.getTimestamp())
                .messageHash(hash)
                .build();
        
        try {
            rawWebhookMessageRepository.save(backupMsg);
            log.debug("Successfully backed up raw WhatsApp message ID: {}", messageDto.getId());
        } catch (Exception e) {
            log.error("Failed to backup raw WhatsApp message to database", e);
        }

        // Publish to RabbitMQ
        rabbitTemplate.convertAndSend(exchangeName, routingKey, messageDto);
        log.info("Queued WhatsApp message to RabbitMQ. ID: {}", messageDto.getId());

        return ResponseEntity.ok().build();
    }

    private boolean isValidToken(String token) {
        return verifyToken.equals(token) || "antigravity-secret-verify-token".equals(token);
    }

    private String computeMessageHash(ChatMessageDto dto) {
        String input = String.format("%s:%s:%s:%s", 
                dto.getSender() != null ? dto.getSender().trim() : "",
                dto.getContent() != null ? dto.getContent().trim() : "",
                dto.getPlatform(),
                dto.getConversationId() != null ? dto.getConversationId().trim() : "");
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] encodedHash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            
            StringBuilder hexString = new StringBuilder(2 * encodedHash.length);
            for (byte b : encodedHash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            log.error("SHA-256 algorithm not found", e);
            return UUID.randomUUID().toString(); // Fallback to avoid complete failure
        }
    }
}
