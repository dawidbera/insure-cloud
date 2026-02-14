package com.insurecloud.policy;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.awspring.cloud.sns.core.SnsTemplate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
@EnableScheduling
public class OutboxProcessor {

    private final OutboxRepository outboxRepository;
    private final SnsTemplate snsTemplate;
    private final ObjectMapper objectMapper;

    /**
     * Periodically processes events from the outbox table.
     * Fetches unprocessed events, publishes them to SNS, and marks them as processed.
     * Running within a transaction ensures that the "mark as processed" state is persisted.
     */
    @Scheduled(fixedDelay = 1000)
    @Transactional
    public void processOutbox() {
        List<OutboxEvent> events = outboxRepository.findByProcessedFalseOrderByCreatedAtAsc();
        
        if (events.isEmpty()) {
            return;
        }

        log.debug("Found {} events in outbox to process", events.size());

        for (OutboxEvent outboxEvent : events) {
            try {
                publishEvent(outboxEvent);
                markAsProcessed(outboxEvent);
            } catch (Exception e) {
                log.error("Failed to process outbox event: {}", outboxEvent.getId(), e);
            }
        }
    }

    /**
     * Deserializes the event payload and publishes it to the designated SNS topic.
     *
     * @param outboxEvent The outbox event record to be published.
     * @throws Exception if deserialization or SNS publishing fails.
     */
    private void publishEvent(OutboxEvent outboxEvent) throws Exception {
        Object eventPayload = objectMapper.readValue(outboxEvent.getPayload(), PolicyIssuedEvent.class);
        log.info("Publishing event from outbox to SNS: {}", outboxEvent.getEventType());
        snsTemplate.sendNotification("policy-issued-topic", eventPayload, outboxEvent.getEventType());
    }

    /**
     * Updates the outbox event record with the processed status and timestamp.
     *
     * @param outboxEvent The outbox event record to be updated.
     */
    private void markAsProcessed(OutboxEvent outboxEvent) {
        outboxEvent.setProcessed(true);
        outboxEvent.setProcessedAt(LocalDateTime.now());
        outboxRepository.save(outboxEvent);
        log.info("Marked outbox event as processed: {}", outboxEvent.getId());
    }
}
