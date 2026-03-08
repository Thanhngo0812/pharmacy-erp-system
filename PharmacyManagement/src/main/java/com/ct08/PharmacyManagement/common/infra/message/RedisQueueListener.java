package com.ct08.PharmacyManagement.common.infra.message;

import com.ct08.PharmacyManagement.common.event.ImageUpdateEvent;
import com.ct08.PharmacyManagement.common.event.OtpEmailEvent;
import com.ct08.PharmacyManagement.common.event.PasswordEmailEvent;
import com.ct08.PharmacyManagement.modules.image.worker.ImageWorker;
import com.ct08.PharmacyManagement.modules.mail.worker.MailWorker;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
@RequiredArgsConstructor
@Slf4j
public class RedisQueueListener {

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;
    private final ImageWorker imageWorker;
    private final MailWorker mailWorker;

    @Scheduled(fixedDelay = 1000)
    public void listenPasswordEmail() {
        processQueue("user-password-email", payload -> {
            try {
                PasswordEmailEvent event = objectMapper.readValue(payload, PasswordEmailEvent.class);
                mailWorker.handlePasswordEmailEvent(event);
            } catch (Exception e) {
                log.error("Failed to process user-password-email: {}", payload, e);
            }
        });
    }

    @Scheduled(fixedDelay = 1000)
    public void listenOtpEmail() {
        processQueue("user-otp-email", payload -> {
            try {
                OtpEmailEvent event = objectMapper.readValue(payload, OtpEmailEvent.class);
                mailWorker.handleOtpEmailEvent(event);
            } catch (Exception e) {
                log.error("Failed to process user-otp-email: {}", payload, e);
            }
        });
    }

    @Scheduled(fixedDelay = 1000)
    public void listenImageUpload() {
        processQueue("employee-image-upload", payload -> {
            try {
                ImageUpdateEvent event = objectMapper.readValue(payload, ImageUpdateEvent.class);
                imageWorker.listen(event);
            } catch (Exception e) {
                log.error("Failed to process employee-image-upload: {}", payload, e);
            }
        });
    }

    private void processQueue(String topic, java.util.function.Consumer<String> processor) {
        try {
            // Block for 1 second max
            String payload = redisTemplate.opsForList().rightPop(topic, Duration.ofSeconds(1));
            if (payload != null) {
                log.info("Received message from topic {}: {}", topic, payload);
                processor.accept(payload);
            }
        } catch (Exception e) {
            log.error("Error popping from Redis Queue for topic {}", topic, e);
        }
    }
}
