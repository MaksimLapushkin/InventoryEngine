package com.inventory.engine.messaging;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.inventory.engine.model.OutboxEvent;
import com.inventory.engine.repository.OutboxEventRepository;
import org.springframework.stereotype.Component;

@Component
public class OutboxOrderLifecycleEventPublisher implements OrderLifecycleEventPublisher {

    private final OutboxEventRepository outboxEventRepository;
    private final ObjectMapper objectMapper;

    public OutboxOrderLifecycleEventPublisher(
            OutboxEventRepository outboxEventRepository,
            ObjectMapper objectMapper
    ) {
        this.outboxEventRepository = outboxEventRepository;
        this.objectMapper = objectMapper;
    }

    @Override
    public void publish(OrderLifecycleEvent event) {
        String payloadJson = serialize(event);
        outboxEventRepository.save(OutboxEvent.forOrderLifecycleEvent(event, payloadJson));
    }

    private String serialize(OrderLifecycleEvent event) {
        try {
            return objectMapper.writeValueAsString(event);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("failed to serialize order lifecycle event", e);
        }
    }
}
