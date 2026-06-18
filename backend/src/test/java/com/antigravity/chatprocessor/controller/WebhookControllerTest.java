package com.antigravity.chatprocessor.controller;

import com.antigravity.chatprocessor.dto.ChatMessageDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(WebhookController.class)
@TestPropertySource(properties = {
        "app.webhook.verify-token=test-token",
        "app.rabbitmq.exchange-name=test-exchange",
        "app.rabbitmq.routing-key=test-routing-key"
})
public class WebhookControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private RabbitTemplate rabbitTemplate;

    @MockitoBean
    private com.antigravity.chatprocessor.repository.RawWebhookMessageRepository rawWebhookMessageRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void whenValidWhatsappChallenge_thenReturns200AndChallengeBody() throws Exception {
        mockMvc.perform(get("/api/v1/webhooks/whatsapp")
                        .param("hub.mode", "subscribe")
                        .param("hub.verify_token", "test-token")
                        .param("hub.challenge", "challenge-code"))
                .andExpect(status().isOk())
                .andExpect(content().string("challenge-code"));
    }

    @Test
    public void whenInvalidWhatsappChallenge_thenReturns403() throws Exception {
        mockMvc.perform(get("/api/v1/webhooks/whatsapp")
                        .param("hub.mode", "subscribe")
                        .param("hub.verify_token", "wrong-token")
                        .param("hub.challenge", "challenge-code"))
                .andExpect(status().isForbidden());
    }

    @Test
    public void whenValidDiscordToken_thenReturns200AndQueuesMessage() throws Exception {
        ChatMessageDto message = ChatMessageDto.builder()
                .sender("User1")
                .content("Hello from Discord")
                .conversationId("conv-1")
                .build();

        mockMvc.perform(post("/api/v1/webhooks/discord")
                        .header("X-Webhook-Token", "test-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(message)))
                .andExpect(status().isOk());

        Mockito.verify(rabbitTemplate).convertAndSend(
                eq("test-exchange"),
                eq("test-routing-key"),
                any(ChatMessageDto.class)
        );
    }

    @Test
    public void whenInvalidDiscordToken_thenReturns401() throws Exception {
        ChatMessageDto message = ChatMessageDto.builder()
                .sender("User1")
                .content("Hello")
                .conversationId("conv-1")
                .build();

        mockMvc.perform(post("/api/v1/webhooks/discord")
                        .header("X-Webhook-Token", "wrong-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(message)))
                .andExpect(status().isUnauthorized());

        Mockito.verifyNoInteractions(rabbitTemplate);
    }

    @Test
    public void whenValidWhatsappToken_thenReturns200AndQueuesMessage() throws Exception {
        ChatMessageDto message = ChatMessageDto.builder()
                .sender("User2")
                .content("Hello from WhatsApp")
                .conversationId("conv-2")
                .build();

        mockMvc.perform(post("/api/v1/webhooks/whatsapp")
                        .header("X-Webhook-Token", "test-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(message)))
                .andExpect(status().isOk());

        Mockito.verify(rabbitTemplate).convertAndSend(
                eq("test-exchange"),
                eq("test-routing-key"),
                any(ChatMessageDto.class)
        );
    }

    @Test
    public void whenDuplicateDiscordMessage_thenReturns200AndDoesNotQueueMessage() throws Exception {
        ChatMessageDto message = ChatMessageDto.builder()
                .sender("User1")
                .content("Hello from Discord")
                .conversationId("conv-1")
                .build();

        Mockito.when(rawWebhookMessageRepository.existsByMessageHash(any(String.class))).thenReturn(true);

        mockMvc.perform(post("/api/v1/webhooks/discord")
                        .header("X-Webhook-Token", "test-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(message)))
                .andExpect(status().isOk());

        Mockito.verifyNoInteractions(rabbitTemplate);
    }

    @Test
    public void whenDuplicateWhatsappMessage_thenReturns200AndDoesNotQueueMessage() throws Exception {
        ChatMessageDto message = ChatMessageDto.builder()
                .sender("User2")
                .content("Hello from WhatsApp")
                .conversationId("conv-2")
                .build();

        Mockito.when(rawWebhookMessageRepository.existsByMessageHash(any(String.class))).thenReturn(true);

        mockMvc.perform(post("/api/v1/webhooks/whatsapp")
                        .header("X-Webhook-Token", "test-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(message)))
                .andExpect(status().isOk());

        Mockito.verifyNoInteractions(rabbitTemplate);
    }

    @Test
    public void whenDiscordSaveThrowsDataIntegrityViolation_thenReturns200AndDoesNotQueueMessage() throws Exception {
        ChatMessageDto message = ChatMessageDto.builder()
                .sender("User1")
                .content("Hello from Discord")
                .conversationId("conv-1")
                .build();

        Mockito.when(rawWebhookMessageRepository.save(any(com.antigravity.chatprocessor.model.RawWebhookMessage.class)))
                .thenThrow(new org.springframework.dao.DataIntegrityViolationException("Duplicate key"));

        mockMvc.perform(post("/api/v1/webhooks/discord")
                        .header("X-Webhook-Token", "test-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(message)))
                .andExpect(status().isOk());

        Mockito.verifyNoInteractions(rabbitTemplate);
    }

    @Test
    public void whenWhatsappSaveThrowsDataIntegrityViolation_thenReturns200AndDoesNotQueueMessage() throws Exception {
        ChatMessageDto message = ChatMessageDto.builder()
                .sender("User2")
                .content("Hello from WhatsApp")
                .conversationId("conv-2")
                .build();

        Mockito.when(rawWebhookMessageRepository.save(any(com.antigravity.chatprocessor.model.RawWebhookMessage.class)))
                .thenThrow(new org.springframework.dao.DataIntegrityViolationException("Duplicate key"));

        mockMvc.perform(post("/api/v1/webhooks/whatsapp")
                        .header("X-Webhook-Token", "test-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(message)))
                .andExpect(status().isOk());

        Mockito.verifyNoInteractions(rabbitTemplate);
    }

    @Test
    public void whenDiscordSaveThrowsGenericException_thenReturns500AndDoesNotQueueMessage() throws Exception {
        ChatMessageDto message = ChatMessageDto.builder()
                .sender("User1")
                .content("Hello from Discord")
                .conversationId("conv-1")
                .build();

        Mockito.when(rawWebhookMessageRepository.save(any(com.antigravity.chatprocessor.model.RawWebhookMessage.class)))
                .thenThrow(new RuntimeException("DB offline"));

        mockMvc.perform(post("/api/v1/webhooks/discord")
                        .header("X-Webhook-Token", "test-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(message)))
                .andExpect(status().isInternalServerError());

        Mockito.verifyNoInteractions(rabbitTemplate);
    }
}
