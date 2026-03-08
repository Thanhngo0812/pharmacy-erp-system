package com.ct08.PharmacyManagement.common.infra.message;

import com.ct08.PharmacyManagement.common.infra.outbox.OutboxEvent;
import com.ct08.PharmacyManagement.common.infra.outbox.OutboxEventRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class MessageProducerService {

    private final OutboxEventRepository outboxEventRepository;
    private final ObjectMapper objectMapper;

    @Transactional
    public void sendMessage(String topic, Object message) {
        try {
            String payload = objectMapper.writeValueAsString(message);
            OutboxEvent event = new OutboxEvent(topic, payload);
            outboxEventRepository.save(event);
            log.info("Saved outbox event for topic {}: {}", topic, payload);
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize message payload for topic {}", topic, e);
            throw new RuntimeException("Could not serialize message to JSON", e);
        }
    }
}
