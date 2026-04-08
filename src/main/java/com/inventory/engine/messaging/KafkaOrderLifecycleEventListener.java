package com.inventory.engine.messaging;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@ConditionalOnProperty(prefix = "app.kafka.order-lifecycle", name = "enabled", havingValue = "true")
public class KafkaOrderLifecycleEventListener {

    private final KafkaTemplate<String, OrderLifecycleEvent> kafkaTemplate;
    private final String topic;

    public KafkaOrderLifecycleEventListener(
            KafkaTemplate<String, OrderLifecycleEvent> kafkaTemplate,
            @Value("${app.kafka.order-lifecycle.topic}") String topic
    ) {
        this.kafkaTemplate = kafkaTemplate;
        this.topic = topic;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onOrderLifecycleEvent(OrderLifecycleEvent event) {
        kafkaTemplate.send(topic, event.orderId().toString(), event);
    }
}
