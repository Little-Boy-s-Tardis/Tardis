package com.antigravity.chatprocessor.service;

import com.antigravity.chatprocessor.dto.ChatMessageDto;
import com.antigravity.chatprocessor.model.AggregatedSummary;
import com.antigravity.chatprocessor.repository.AggregatedSummaryRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class AISummarizerService {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(AISummarizerService.class);

    private final AggregatedSummaryRepository aggregatedSummaryRepository;
    private final SimpMessagingTemplate messagingTemplate;

    public AISummarizerService(AggregatedSummaryRepository aggregatedSummaryRepository, SimpMessagingTemplate messagingTemplate) {
        this.aggregatedSummaryRepository = aggregatedSummaryRepository;
        this.messagingTemplate = messagingTemplate;
    }

    @Value("${app.qwen.api-key:}")
    private String qwenApiKey;

    @Value("${app.qwen.url}")
    private String qwenUrl;

    @Value("${app.qwen.model}")
    private String qwenModel;

    private final RestTemplate restTemplate = new RestTemplate();

    public void summarizeBatch(String conversationId, List<ChatMessageDto> messages) {
        log.info("AISummarizerService received batch for conversationId: {} with {} messages", 
                conversationId, messages.size());

        if (messages == null || messages.isEmpty()) {
            return;
        }

        String summaryText = "";

        // Attempt calling Qwen API if API key is provided
        if (qwenApiKey != null && !qwenApiKey.trim().isEmpty()) {
            try {
                summaryText = callQwenAPI(messages);
            } catch (Exception e) {
                log.error("Failed to generate summary with Qwen API. Falling back to local smart summarizer.", e);
                summaryText = generateSmartFallbackSummary(messages);
            }
        } else {
            log.info("Qwen API key is empty. Using smart local summarizer.");
            summaryText = generateSmartFallbackSummary(messages);
        }

        // Generate summary ID and comma-separated original message IDs
        String summaryId = UUID.randomUUID().toString();
        String originalIds = messages.stream()
                .map(ChatMessageDto::getId)
                .collect(Collectors.joining(","));

        // Save to Database
        AggregatedSummary aggregatedSummary = AggregatedSummary.builder()
                .id(summaryId)
                .conversationId(conversationId)
                .summaryText(summaryText)
                .timestamp(Instant.now())
                .originalMessageIds(originalIds)
                .build();

        try {
            aggregatedSummaryRepository.save(aggregatedSummary);
            log.info("Successfully persisted AggregatedSummary ID: {}", summaryId);
        } catch (Exception e) {
            log.error("Failed to persist summary in PostgreSQL", e);
        }

        // Broadcast to WebSocket clients
        Map<String, Object> wsResponse = buildWebSocketPayload(aggregatedSummary, messages);
        try {
            messagingTemplate.convertAndSend("/topic/announcements", wsResponse);
            log.info("Successfully broadcasted summary via WebSockets to /topic/announcements");
        } catch (Exception e) {
            log.error("Failed to broadcast WebSocket announcement", e);
        }
    }

    private String callQwenAPI(List<ChatMessageDto> messages) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + qwenApiKey);
        headers.set("HTTP-Referer", "https://tardis-hazel.vercel.app");
        headers.set("X-Title", "Tardis Aggregator");
        headers.set("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36");

        StringBuilder contentBuilder = new StringBuilder("Announcements to summarize:\n");
        for (int i = 0; i < messages.size(); i++) {
            ChatMessageDto msg = messages.get(i);
            contentBuilder.append(String.format("%d. [%s] %s: %s\n", 
                    i + 1, msg.getPlatform(), msg.getSender(), msg.getContent()));
        }

        // Build request body for OpenAI-compatible Qwen API
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", qwenModel);
        
        List<Map<String, String>> chatMessages = new ArrayList<>();
        
        Map<String, String> systemMsg = new HashMap<>();
        systemMsg.put("role", "system");
        systemMsg.put("content", "You are a contest management assistant. Group and summarize the following announcements from judges into a concise list of bullet points in English. Focus on: deadlines, technical requirements, and key notes. Do not start bullet points with emojis.");
        chatMessages.add(systemMsg);

        Map<String, String> userMsg = new HashMap<>();
        userMsg.put("role", "user");
        userMsg.put("content", contentBuilder.toString());
        chatMessages.add(userMsg);

        requestBody.put("messages", chatMessages);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
        
        log.debug("Sending request to Qwen API: {}", qwenUrl);
        ResponseEntity<Map> response = restTemplate.postForEntity(qwenUrl, entity, Map.class);
        
        if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
            Map body = response.getBody();
            List choices = (List) body.get("choices");
            if (choices != null && !choices.isEmpty()) {
                Map choice = (Map) choices.get(0);
                Map message = (Map) choice.get("message");
                if (message != null && message.get("content") != null) {
                    return (String) message.get("content");
                }
            }
        }
        throw new RuntimeException("Unexpected response from Qwen API");
    }

    private String generateSmartFallbackSummary(List<ChatMessageDto> messages) {
        List<String> bullets = new ArrayList<>();
        
        for (ChatMessageDto msg : messages) {
            String content = msg.getContent();
            String contentLower = content.toLowerCase();
            String sender = msg.getSender();

            if (contentLower.contains("deadline") || contentLower.contains("postponed") || contentLower.contains("postpone") || contentLower.contains("extend") || contentLower.contains("extension")) {
                bullets.add(String.format("Deadline Extension: Announcement from %s regarding timeline: \"%s\"", sender, extractKeySentence(content)));
            } else if (contentLower.contains("docker") || contentLower.contains("api") || contentLower.contains("port") || contentLower.contains("db") || contentLower.contains("cors")) {
                bullets.add(String.format("Technical Requirement: %s reminded about configuration: \"%s\"", sender, extractKeySentence(content)));
            } else {
                bullets.add(String.format("Key Note from %s: \"%s\"", sender, extractKeySentence(content)));
            }
        }

        // Deduplicate summary bullets if identical
        List<String> uniqueBullets = bullets.stream().distinct().collect(Collectors.toList());
        return String.join("\n", uniqueBullets);
    }

    private String extractKeySentence(String content) {
        if (content == null || content.trim().isEmpty()) {
            return "";
        }
        String[] sentences = content.split("[.!?\n]+");
        for (String sentence : sentences) {
            String clean = sentence.trim();
            if (clean.length() > 10) {
                return clean;
            }
        }
        return content.trim();
    }

    private Map<String, Object> buildWebSocketPayload(AggregatedSummary summary, List<ChatMessageDto> originals) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", summary.getId());
        map.put("timestamp", "Just now");
        map.put("conversationId", summary.getConversationId());

        // Summaries list
        List<String> bullets = new ArrayList<>();
        if (summary.getSummaryText() != null) {
            for (String line : summary.getSummaryText().split("\n")) {
                if (!line.trim().isEmpty()) {
                    bullets.add(line.trim());
                }
            }
        }
        map.put("aiSummary", bullets);

        // Map originals to simplified objects
        map.put("originalMessages", originals);

        // Combined original text
        StringBuilder combinedContent = new StringBuilder();
        for (int i = 0; i < originals.size(); i++) {
            if (i > 0) {
                combinedContent.append("\n\n");
            }
            ChatMessageDto msg = originals.get(i);
            combinedContent.append("[").append(msg.getPlatform()).append("] ")
                    .append(msg.getSender()).append(": ").append(msg.getContent());
        }
        map.put("originalMessage", combinedContent.toString());

        // Heuristics
        String sender = "Judges (Aggregated)";
        String platform = "DISCORD";
        String importance = "MEDIUM";
        Set<String> tags = new HashSet<>();
        tags.add("summary");

        if (!originals.isEmpty()) {
            if (originals.size() == 1) {
                sender = originals.get(0).getSender();
                platform = originals.get(0).getPlatform();
            } else {
                StringBuilder senders = new StringBuilder();
                for (int i = 0; i < Math.min(originals.size(), 2); i++) {
                    if (i > 0) senders.append(", ");
                    senders.append(originals.get(i).getSender());
                }
                if (originals.size() > 2) {
                    senders.append(" +").append(originals.size() - 2);
                }
                sender = "Judges: " + senders.toString();
                
                boolean hasDiscord = originals.stream().anyMatch(m -> "DISCORD".equalsIgnoreCase(m.getPlatform()));
                boolean hasWhatsapp = originals.stream().anyMatch(m -> "WHATSAPP".equalsIgnoreCase(m.getPlatform()));
                if (hasDiscord && hasWhatsapp) {
                    platform = "DISCORD";
                } else if (hasWhatsapp) {
                    platform = "WHATSAPP";
                }
            }

            boolean hasHigh = false;
            boolean hasLow = false;
            String explicitImportance = null;

            for (ChatMessageDto msg : originals) {
                if (msg.getImportance() != null && !msg.getImportance().trim().isEmpty()) {
                    explicitImportance = msg.getImportance().toUpperCase();
                }

                String content = msg.getContent().toLowerCase();
                if (content.contains("deadline") || content.contains("urgent") || content.contains("emergency") || content.contains("critical") || content.contains("important") || content.contains("reminder")) {
                    hasHigh = true;
                } else if (content.contains("minor") || content.contains("fyi") || content.contains("trivial") || content.contains("optional") || content.contains("info")) {
                    hasLow = true;
                }

                if (content.contains("deadline")) tags.add("deadline");
                if (content.contains("rules") || content.contains("rule") || content.contains("guideline")) tags.add("rules");
                if (content.contains("docker") || content.contains("compose")) tags.add("docker");
                if (content.contains("api") || content.contains("spring")) tags.add("backend");
                if (content.contains("ui") || content.contains("ux")) tags.add("frontend");
            }

            if (explicitImportance != null && (explicitImportance.equals("HIGH") || explicitImportance.equals("MEDIUM") || explicitImportance.equals("LOW"))) {
                importance = explicitImportance;
            } else if (hasHigh) {
                importance = "HIGH";
            } else if (hasLow) {
                importance = "LOW";
            }
        }

        map.put("sender", sender);
        map.put("platform", platform);
        map.put("importance", importance);
        map.put("tags", new ArrayList<>(tags));

        return map;
    }
}
