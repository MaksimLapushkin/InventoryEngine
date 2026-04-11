package com.inventory.engine.repository;

import com.inventory.engine.messaging.OrderLifecycleEvent;
import com.inventory.engine.messaging.OrderLifecycleEventType;
import com.inventory.engine.messaging.OrderLifecyclePayload;
import com.inventory.engine.messaging.OrderLinePayload;
import com.inventory.engine.model.OutboxEvent;
import com.inventory.engine.model.OutboxEventStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class OutboxEventRepositoryTest {

    private static final Instant NOW = Instant.parse("2026-04-11T10:15:30Z");

    @Autowired
    private OutboxEventRepository outboxEventRepository;

    @Test
    void shouldFindFailedEventsWhenNextAttemptIsDue() {
        OutboxEvent dueFailedEvent = outboxEvent(101L);
        dueFailedEvent.markFailed("previous failure", NOW.minusSeconds(1));

        OutboxEvent notDueFailedEvent = outboxEvent(102L);
        notDueFailedEvent.markFailed("previous failure", NOW.plusSeconds(60));

        outboxEventRepository.saveAll(List.of(dueFailedEvent, notDueFailedEvent));
        outboxEventRepository.flush();

        List<OutboxEvent> retryableEvents = outboxEventRepository.findRetryableEvents(
                List.of(OutboxEventStatus.NEW, OutboxEventStatus.FAILED),
                NOW,
                5,
                PageRequest.of(0, 10)
        );

        assertThat(retryableEvents)
                .extracting(OutboxEvent::getEventId)
                .contains(dueFailedEvent.getEventId())
                .doesNotContain(notDueFailedEvent.getEventId());
    }

    @Test
    void shouldNotFindFailedEventsAfterMaxAttempts() {
        OutboxEvent exhaustedFailedEvent = outboxEvent(103L);
        for (int i = 0; i < 5; i++) {
            exhaustedFailedEvent.markFailed("previous failure", NOW.minusSeconds(1));
        }

        outboxEventRepository.saveAndFlush(exhaustedFailedEvent);

        List<OutboxEvent> retryableEvents = outboxEventRepository.findRetryableEvents(
                List.of(OutboxEventStatus.NEW, OutboxEventStatus.FAILED),
                NOW,
                5,
                PageRequest.of(0, 10)
        );

        assertThat(retryableEvents)
                .extracting(OutboxEvent::getEventId)
                .doesNotContain(exhaustedFailedEvent.getEventId());
    }

    private OutboxEvent outboxEvent(Long aggregateId) {
        OrderLifecycleEvent event = new OrderLifecycleEvent(
                UUID.randomUUID(),
                aggregateId,
                "correlation-" + aggregateId,
                NOW.minusSeconds(5),
                OrderLifecycleEventType.ORDER_CREATED,
                new OrderLifecyclePayload(
                        aggregateId,
                        "CREATED",
                        null,
                        List.of(new OrderLinePayload(7L, 2))
                )
        );
        return OutboxEvent.forOrderLifecycleEvent(event, "{}");
    }
}
