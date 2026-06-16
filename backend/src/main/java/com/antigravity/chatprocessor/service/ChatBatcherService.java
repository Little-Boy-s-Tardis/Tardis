package com.antigravity.chatprocessor.service;

import com.antigravity.chatprocessor.dto.ChatMessageDto;
import jakarta.annotation.PreDestroy;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

@Service
public class ChatBatcherService {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ChatBatcherService.class);

    private final AISummarizerService aiSummarizerService;
    private final int maxDelayMinutes;
    private final int silenceDelaySeconds;

    private final ConcurrentHashMap<String, BatchState> activeBatches = new ConcurrentHashMap<>();
    private final ScheduledExecutorService scheduler;

    public ChatBatcherService(
            AISummarizerService aiSummarizerService,
            @Value("${app.batch.max-delay-minutes}") int maxDelayMinutes,
            @Value("${app.batch.silence-delay-seconds}") int silenceDelaySeconds,
            @Value("${app.batch.thread-pool-size}") int threadPoolSize) {
        this.aiSummarizerService = aiSummarizerService;
        this.maxDelayMinutes = maxDelayMinutes;
        this.silenceDelaySeconds = silenceDelaySeconds;
        this.scheduler = Executors.newScheduledThreadPool(threadPoolSize);
        log.info("Initialized ChatBatcherService with thread pool size: {}", threadPoolSize);
    }

    public void addMessageToBatch(ChatMessageDto message) {
        String rawConvId = message.getConversationId();
        final String conversationId = (rawConvId == null || rawConvId.trim().isEmpty()) ? "global-contest" : rawConvId;
        message.setConversationId(conversationId);

        activeBatches.compute(conversationId, (key, existingBatch) -> {
            BatchState batch = existingBatch;
            if (batch == null) {
                batch = new BatchState();
                log.info("Starting new batch for conversationId: {}", conversationId);
                // Schedule max delay task since this is the first message in the batch
                ScheduledFuture<?> maxDelayTask = scheduler.schedule(
                        () -> triggerBatchProcessing(conversationId, "MAX_DELAY"),
                        maxDelayMinutes,
                        TimeUnit.MINUTES
                );
                batch.maxDelayTask = maxDelayTask;
            }

            batch.messages.add(message);
            batch.lastMessageTime = System.currentTimeMillis();

            // Cancel existing silence task if any (debounce)
            if (batch.silenceTask != null) {
                batch.silenceTask.cancel(false);
            }

            // Reschedule silence task
            ScheduledFuture<?> silenceTask = scheduler.schedule(
                    () -> triggerBatchProcessing(conversationId, "SILENCE"),
                    silenceDelaySeconds,
                    TimeUnit.SECONDS
            );
            batch.silenceTask = silenceTask;

            return batch;
        });
    }

    private void triggerBatchProcessing(String conversationId, String triggerType) {
        BatchState batchState = activeBatches.remove(conversationId);
        if (batchState == null) {
            // Already processed by another trigger
            return;
        }

        log.info("Triggering batch summarization for conversationId: {} due to: {}. Batch size: {}",
                conversationId, triggerType, batchState.messages.size());

        // Cancel tasks to release resources
        if (batchState.silenceTask != null) {
            batchState.silenceTask.cancel(false);
        }
        if (batchState.maxDelayTask != null) {
            batchState.maxDelayTask.cancel(false);
        }

        // Copy list to avoid concurrent issues and pass to summarizer
        List<ChatMessageDto> messagesToProcess = new ArrayList<>(batchState.messages);
        
        // Execute asynchronously so scheduler threads are not blocked
        CompletableFuture.runAsync(() -> aiSummarizerService.summarizeBatch(conversationId, messagesToProcess));
    }

    @PreDestroy
    public void shutdown() {
        log.info("Shutting down ChatBatcherService scheduler...");
        scheduler.shutdown();
        try {
            if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            scheduler.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    private static class BatchState {
        final List<ChatMessageDto> messages = new CopyOnWriteArrayList<>();
        final long firstMessageTime = System.currentTimeMillis();
        volatile long lastMessageTime = System.currentTimeMillis();
        ScheduledFuture<?> silenceTask;
        ScheduledFuture<?> maxDelayTask;
    }
}
