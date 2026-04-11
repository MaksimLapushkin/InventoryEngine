package com.inventory.engine.messaging;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.inventory.engine.model.OutboxEvent;
import com.inventory.engine.model.OutboxEventStatus;
import com.inventory.engine.repository.OutboxEventRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.domain.PageRequest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.ExecutionException;

@Component
@ConditionalOnProperty(prefix = "app.kafka.order-lifecycle", name = "enabled", havingValue = "true")
public class OutboxEventKafkaPublisher {

    private static final int MAX_ERROR_MESSAGE_LENGTH = 1000;

    private final OutboxEventRepository outboxEventRepository;
    private final KafkaTemplate<String, OrderLifecycleEvent> kafkaTemplate;
    private final ObjectMapper objectMapper;
    private final String topic;
    private final int batchSize;

    public OutboxEventKafkaPublisher(
            OutboxEventRepository outboxEventRepository,
            KafkaTemplate<String, OrderLifecycleEvent> kafkaTemplate,
            ObjectMapper objectMapper,
            @Value("${app.kafka.order-lifecycle.topic}") String topic,
            @Value("${app.outbox.publisher.batch-size:50}") int batchSize
    ) {
        this.outboxEventRepository = outboxEventRepository;
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
        this.topic = topic;
        this.batchSize = Math.max(batchSize, 1);
    }

    @Scheduled(fixedDelayString = "${app.outbox.publisher.fixed-delay-ms:5000}")
    @Transactional
    public void publishNewEvents() {
        List<OutboxEvent> events = outboxEventRepository.findByStatusOrderByCreatedAtAsc(
                OutboxEventStatus.NEW,
                PageRequest.of(0, batchSize)
        );

        for (OutboxEvent event : events) {
            publish(event);
        }
    }

    private void publish(OutboxEvent outboxEvent) {
        try {
            OrderLifecycleEvent event = deserialize(outboxEvent);
            kafkaTemplate.send(topic, outboxEvent.getAggregateId(), event).get();
            outboxEvent.markPublished();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            outboxEvent.markFailed(errorMessage(e));
        } catch (ExecutionException | RuntimeException e) {
            outboxEvent.markFailed(errorMessage(e));
        }
    }

    private OrderLifecycleEvent deserialize(OutboxEvent outboxEvent) {
        try {
            return objectMapper.readValue(outboxEvent.getPayloadJson(), OrderLifecycleEvent.class);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("failed to deserialize outbox event " + outboxEvent.getId(), e);
        }
    }

    private String errorMessage(Exception e) {
        Throwable cause = e.getCause() == null ? e : e.getCause();
        String message = cause.getMessage();
        if (message == null || message.isBlank()) {
            message = cause.getClass().getName();
        }
        if (message.length() > MAX_ERROR_MESSAGE_LENGTH) {
            return message.substring(0, MAX_ERROR_MESSAGE_LENGTH);
        }
        return message;
    }
}
