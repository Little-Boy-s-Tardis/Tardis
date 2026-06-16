package com.antigravity.chatprocessor.service;

import com.antigravity.chatprocessor.dto.ChatMessageDto;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

public class ChatBatcherServiceTest {

    private AISummarizerService aiSummarizerService;
    private ChatBatcherService chatBatcherService;

    @BeforeEach
    public void setUp() {
        aiSummarizerService = mock(AISummarizerService.class);
        // Set silence threshold to 1 second, max delay to 1 minute, and thread pool size of 2 for rapid testing
        chatBatcherService = new ChatBatcherService(aiSummarizerService, 1, 1, 2);
    }

    @AfterEach
    public void tearDown() {
        chatBatcherService.shutdown();
    }

    @Test
    public void whenMessageIsAdded_andSilenceThresholdPasses_thenBatchIsProcessed() throws Exception {
        ChatMessageDto msg1 = ChatMessageDto.builder()
                .id("1")
                .sender("UserA")
                .content("Hello")
                .conversationId("conv-abc")
                .timestamp(Instant.now())
                .build();

        ChatMessageDto msg2 = ChatMessageDto.builder()
                .id("2")
                .sender("UserA")
                .content("How are you?")
                .conversationId("conv-abc")
                .timestamp(Instant.now())
                .build();

        chatBatcherService.addMessageToBatch(msg1);
        chatBatcherService.addMessageToBatch(msg2);

        // Verify that summarization HAS NOT been called yet
        verify(aiSummarizerService, never()).summarizeBatch(any(), any());

        // Wait for silence threshold (1 second) to expire
        Thread.sleep(1500);

        // Verify that it HAS been called exactly once with our messages
        verify(aiSummarizerService, times(1)).summarizeBatch(eq("conv-abc"), any(List.class));
    }

    @Test
    public void whenMultipleConversations_thenBatchesAreScopedCorrectly() throws Exception {
        ChatMessageDto msgA = ChatMessageDto.builder()
                .id("1")
                .sender("UserA")
                .content("Hello")
                .conversationId("conv-A")
                .build();

        ChatMessageDto msgB = ChatMessageDto.builder()
                .id("2")
                .sender("UserB")
                .content("Hi")
                .conversationId("conv-B")
                .build();

        chatBatcherService.addMessageToBatch(msgA);
        chatBatcherService.addMessageToBatch(msgB);

        // Wait for silence threshold (1 second)
        Thread.sleep(1500);

        // Verify both batches were triggered separately
        verify(aiSummarizerService, times(1)).summarizeBatch(eq("conv-A"), any(List.class));
        verify(aiSummarizerService, times(1)).summarizeBatch(eq("conv-B"), any(List.class));
    }
}
