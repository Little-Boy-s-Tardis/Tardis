package com.antigravity.chatprocessor.model;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "raw_webhook_messages", indexes = {
    @Index(name = "idx_message_hash", columnList = "messageHash")
})
public class RawWebhookMessage {

    @Id
    @Column(length = 255)
    private String id;

    @Column(nullable = false)
    private String sender;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(nullable = false, length = 20)
    private String platform; // DISCORD, WHATSAPP

    @Column(name = "conversation_id")
    private String conversationId;

    @Column(nullable = false)
    private Instant timestamp;

    @Column(name = "message_hash", nullable = false, length = 64)
    private String messageHash;

    public RawWebhookMessage() {}

    public RawWebhookMessage(String id, String sender, String content, String platform, String conversationId, Instant timestamp, String messageHash) {
        this.id = id;
        this.sender = sender;
        this.content = content;
        this.platform = platform;
        this.conversationId = conversationId;
        this.timestamp = timestamp;
        this.messageHash = messageHash;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getSender() { return sender; }
    public void setSender(String sender) { this.sender = sender; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public String getPlatform() { return platform; }
    public void setPlatform(String platform) { this.platform = platform; }

    public String getConversationId() { return conversationId; }
    public void setConversationId(String conversationId) { this.conversationId = conversationId; }

    public Instant getTimestamp() { return timestamp; }
    public void setTimestamp(Instant timestamp) { this.timestamp = timestamp; }

    public String getMessageHash() { return messageHash; }
    public void setMessageHash(String messageHash) { this.messageHash = messageHash; }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String id;
        private String sender;
        private String content;
        private String platform;
        private String conversationId;
        private Instant timestamp;
        private String messageHash;

        public Builder id(String id) { this.id = id; return this; }
        public Builder sender(String sender) { this.sender = sender; return this; }
        public Builder content(String content) { this.content = content; return this; }
        public Builder platform(String platform) { this.platform = platform; return this; }
        public Builder conversationId(String conversationId) { this.conversationId = conversationId; return this; }
        public Builder timestamp(Instant timestamp) { this.timestamp = timestamp; return this; }
        public Builder messageHash(String messageHash) { this.messageHash = messageHash; return this; }

        public RawWebhookMessage build() {
            return new RawWebhookMessage(id, sender, content, platform, conversationId, timestamp, messageHash);
        }
    }
}
