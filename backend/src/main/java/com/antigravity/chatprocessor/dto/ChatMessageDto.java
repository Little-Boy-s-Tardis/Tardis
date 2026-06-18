package com.antigravity.chatprocessor.dto;

import java.io.Serializable;
import java.time.Instant;

public class ChatMessageDto implements Serializable {
    private static final long serialVersionUID = 1L;

    private String id;
    private String sender;
    private String content;
    private String platform; // DISCORD, WHATSAPP
    private String conversationId;
    private Instant timestamp;
    private String importance; // HIGH, MEDIUM, LOW

    public ChatMessageDto() {}

    public ChatMessageDto(String id, String sender, String content, String platform, String conversationId, Instant timestamp, String importance) {
        this.id = id;
        this.sender = sender;
        this.content = content;
        this.platform = platform;
        this.conversationId = conversationId;
        this.timestamp = timestamp;
        this.importance = importance;
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

    public String getImportance() { return importance; }
    public void setImportance(String importance) { this.importance = importance; }

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
        private String importance;

        public Builder id(String id) { this.id = id; return this; }
        public Builder sender(String sender) { this.sender = sender; return this; }
        public Builder content(String content) { this.content = content; return this; }
        public Builder platform(String platform) { this.platform = platform; return this; }
        public Builder conversationId(String conversationId) { this.conversationId = conversationId; return this; }
        public Builder timestamp(Instant timestamp) { this.timestamp = timestamp; return this; }
        public Builder importance(String importance) { this.importance = importance; return this; }

        public ChatMessageDto build() {
            return new ChatMessageDto(id, sender, content, platform, conversationId, timestamp, importance);
        }
    }
}
