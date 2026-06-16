package com.antigravity.chatprocessor.controller;

import com.antigravity.chatprocessor.model.AggregatedSummary;
import com.antigravity.chatprocessor.model.RawWebhookMessage;
import com.antigravity.chatprocessor.repository.AggregatedSummaryRepository;
import com.antigravity.chatprocessor.repository.RawWebhookMessageRepository;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;

@RestController
@RequestMapping("/api/v1/announcements")
public class DashboardController {

    private final AggregatedSummaryRepository aggregatedSummaryRepository;
    private final RawWebhookMessageRepository rawWebhookMessageRepository;

    public DashboardController(AggregatedSummaryRepository aggregatedSummaryRepository, RawWebhookMessageRepository rawWebhookMessageRepository) {
        this.aggregatedSummaryRepository = aggregatedSummaryRepository;
        this.rawWebhookMessageRepository = rawWebhookMessageRepository;
    }

    @GetMapping
    public List<Map<String, Object>> getAnnouncements() {
        List<AggregatedSummary> summaries = aggregatedSummaryRepository.findAllByOrderByTimestampDesc();
        List<Map<String, Object>> response = new ArrayList<>();

        for (AggregatedSummary summary : summaries) {
            Map<String, Object> map = new HashMap<>();
            map.put("id", summary.getId());
            map.put("timestamp", summary.getTimestamp());
            map.put("conversationId", summary.getConversationId());

            // Process summaries into bullet points (split by newline)
            List<String> bullets = new ArrayList<>();
            if (summary.getSummaryText() != null) {
                for (String line : summary.getSummaryText().split("\n")) {
                    if (!line.trim().isEmpty()) {
                        bullets.add(line.trim());
                    }
                }
            }
            map.put("aiSummary", bullets);

            // Fetch original messages
            List<RawWebhookMessage> originals = new ArrayList<>();
            if (summary.getOriginalMessageIds() != null && !summary.getOriginalMessageIds().trim().isEmpty()) {
                List<String> ids = Arrays.asList(summary.getOriginalMessageIds().split(","));
                originals = rawWebhookMessageRepository.findAllByIdIn(ids);
            }
            map.put("originalMessages", originals);

            // Consolidate original messages content
            StringBuilder combinedContent = new StringBuilder();
            for (int i = 0; i < originals.size(); i++) {
                if (i > 0) {
                    combinedContent.append("\n\n");
                }
                RawWebhookMessage msg = originals.get(i);
                combinedContent.append("[").append(msg.getPlatform()).append("] ")
                        .append(msg.getSender()).append(": ").append(msg.getContent());
            }
            map.put("originalMessage", combinedContent.toString());

            // Heuristics for sender name, platform and importance
            String sender = "Judges (Aggregated)";
            String platform = "DISCORD"; // Default fallback
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
                    
                    // If multiple platforms, flag as both
                    boolean hasDiscord = originals.stream().anyMatch(m -> "DISCORD".equalsIgnoreCase(m.getPlatform()));
                    boolean hasWhatsapp = originals.stream().anyMatch(m -> "WHATSAPP".equalsIgnoreCase(m.getPlatform()));
                    if (hasDiscord && hasWhatsapp) {
                        platform = "DISCORD"; // UI will show discord style, but original messages reveal both
                    } else if (hasWhatsapp) {
                        platform = "WHATSAPP";
                    }
                }

                // Check if any raw message contains urgent keywords
                boolean hasHigh = false;
                for (RawWebhookMessage msg : originals) {
                    String content = msg.getContent().toLowerCase();
                    if (content.contains("deadline") || content.contains("urgent") || content.contains("emergency") || content.contains("critical")) {
                        hasHigh = true;
                    }
                    
                    // Basic tag mining
                    if (content.contains("deadline")) tags.add("deadline");
                    if (content.contains("rules") || content.contains("rule") || content.contains("guideline")) tags.add("rules");
                    if (content.contains("docker") || content.contains("compose")) tags.add("docker");
                    if (content.contains("api") || content.contains("spring")) tags.add("backend");
                    if (content.contains("ui") || content.contains("ux")) tags.add("frontend");
                }
                if (hasHigh) {
                    importance = "HIGH";
                }
            }

            map.put("sender", sender);
            map.put("platform", platform);
            map.put("importance", importance);
            map.put("tags", new ArrayList<>(tags));

            response.add(map);
        }

        return response;
    }

    @org.springframework.web.bind.annotation.DeleteMapping
    public void clearAnnouncements() {
        aggregatedSummaryRepository.deleteAll();
        rawWebhookMessageRepository.deleteAll();
    }
}
