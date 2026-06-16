package com.antigravity.chatprocessor.model;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "aggregated_summaries")
public class AggregatedSummary {

    @Id
    @Column(length = 36)
    private String id;

    @Column(name = "conversation_id")
    private String conversationId;

    @Column(name = "summary_text", nullable = false, columnDefinition = "TEXT")
    private String summaryText;

    @Column(nullable = false)
    private Instant timestamp;

    @Column(name = "original_message_ids", columnDefinition = "TEXT")
    private String originalMessageIds; // Comma-separated list of RawWebhookMessage IDs

    public AggregatedSummary() {}

    public AggregatedSummary(String id, String conversationId, String summaryText, Instant timestamp, String originalMessageIds) {
        this.id = id;
        this.conversationId = conversationId;
        this.summaryText = summaryText;
        this.timestamp = timestamp;
        this.originalMessageIds = originalMessageIds;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getConversationId() { return conversationId; }
    public void setConversationId(String conversationId) { this.conversationId = conversationId; }

    public String getSummaryText() { return summaryText; }
    public void setSummaryText(String summaryText) { this.summaryText = summaryText; }

    public Instant getTimestamp() { return timestamp; }
    public void setTimestamp(Instant timestamp) { this.timestamp = timestamp; }

    public String getOriginalMessageIds() { return originalMessageIds; }
    public void setOriginalMessageIds(String originalMessageIds) { this.originalMessageIds = originalMessageIds; }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String id;
        private String conversationId;
        private String summaryText;
        private Instant timestamp;
        private String originalMessageIds;

        public Builder id(String id) { this.id = id; return this; }
        public Builder conversationId(String conversationId) { this.conversationId = conversationId; return this; }
        public Builder summaryText(String summaryText) { this.summaryText = summaryText; return this; }
        public Builder timestamp(Instant timestamp) { this.timestamp = timestamp; return this; }
        public Builder originalMessageIds(String originalMessageIds) { this.originalMessageIds = originalMessageIds; return this; }

        public AggregatedSummary build() {
            return new AggregatedSummary(id, conversationId, summaryText, timestamp, originalMessageIds);
        }
    }
}
