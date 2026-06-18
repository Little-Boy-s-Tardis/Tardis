package com.antigravity.chatprocessor.repository;

import com.antigravity.chatprocessor.model.RawWebhookMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public interface RawWebhookMessageRepository extends JpaRepository<RawWebhookMessage, String> {
    
    /**
     * Checks if a message hash already exists in the database.
     * Used for deduplicating webhook calls.
     */
    boolean existsByMessageHash(String messageHash);

    /**
     * Find messages by IDs.
     */
    List<RawWebhookMessage> findAllByIdIn(List<String> ids);
}
