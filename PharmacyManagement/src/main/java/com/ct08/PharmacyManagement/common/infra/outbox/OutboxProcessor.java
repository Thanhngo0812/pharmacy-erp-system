package com.ct08.PharmacyManagement.common.infra.outbox;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class OutboxProcessor {

    private final OutboxEventRepository outboxEventRepository;
    private final StringRedisTemplate redisTemplate;

    @Scheduled(fixedDelay = 5000)
    public void processOutboxEvents() {
        List<OutboxEvent> events = outboxEventRepository.findAllByOrderByCreatedAtAsc();
        if (events.isEmpty()) {
            return;
        }

        log.info("Processing {} outbox events...", events.size());

        for (OutboxEvent event : events) {
            try {
                // Redis Queue push
                redisTemplate.opsForList().leftPush(event.getTopic(), event.getPayload());
                // Remove from local database after successfully pushing to Redis
                outboxEventRepository.delete(event);
            } catch (Exception e) {
                log.error("Failed to process event with id {}: {}", event.getId(), e.getMessage());
                // Stop processing further events to retain order, or continue depending on
                // strictness
                // We'll break here to avoid pushing newer events before older ones in case of
                // Redis failure
                break;
            }
        }
    }
}
