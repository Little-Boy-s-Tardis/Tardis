package com.antigravity.chatprocessor.repository;

import com.antigravity.chatprocessor.model.RawWebhookMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public interface RawWebhookMessageRepository extends JpaRepository<RawWebhookMessage, String> {
    
    /**
     * Counts the number of times a message hash appears after a given timestamp.
     * Used for deduplicating webhook calls.
     */
    int countByMessageHashAndTimestampAfter(String messageHash, Instant timestamp);

    /**
     * Find messages by IDs.
     */
    List<RawWebhookMessage> findAllByIdIn(List<String> ids);
}
