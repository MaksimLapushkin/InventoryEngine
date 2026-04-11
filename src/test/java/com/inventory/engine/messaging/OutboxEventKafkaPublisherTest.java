package com.inventory.engine.messaging;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.inventory.engine.model.OutboxEvent;
import com.inventory.engine.model.OutboxEventStatus;
import com.inventory.engine.repository.OutboxEventRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OutboxEventKafkaPublisherTest {

    private static final String TOPIC = "order.lifecycle.v1";
    private static final Instant NOW = Instant.parse("2026-04-11T10:15:30Z");
    private static final List<OutboxEventStatus> RETRYABLE_STATUSES = List.of(
            OutboxEventStatus.NEW,
            OutboxEventStatus.IN_PROGRESS,
            OutboxEventStatus.FAILED
    );

    @Mock
    private OutboxEventRepository outboxEventRepository;

    @Mock
    private KafkaTemplate<String, OrderLifecycleEvent> kafkaTemplate;

    private ObjectMapper objectMapper;
    private OutboxEventKafkaPublisher publisher;

    @BeforeEach
    void setUp() {
        objectMapper = JsonMapper.builder()
                .findAndAddModules()
                .build();
        publisher = new OutboxEventKafkaPublisher(
                outboxEventRepository,
                kafkaTemplate,
                objectMapper,
                TOPIC,
                10,
                5,
                Clock.fixed(NOW, ZoneOffset.UTC)
        );
    }

    @Test
    void shouldMarkClaimedEventPublishedOnSuccessfulKafkaSend() {
        OutboxEvent outboxEvent = outboxEvent();
        setId(outboxEvent, 1L);

        when(outboxEventRepository.findRetryableEvents(
                eq(RETRYABLE_STATUSES),
                eq(NOW),
                eq(5),
                any(Pageable.class)
        )).thenReturn(List.of(outboxEvent));
        when(outboxEventRepository.findById(1L)).thenReturn(Optional.of(outboxEvent));
        when(kafkaTemplate.send(eq(TOPIC), eq("99"), any(OrderLifecycleEvent.class)))
                .thenAnswer(invocation -> {
                    assertThat(outboxEvent.getStatus()).isEqualTo(OutboxEventStatus.IN_PROGRESS);
                    assertThat(outboxEvent.getNextAttemptAt()).isEqualTo(NOW.plusSeconds(60));
                    return CompletableFuture.completedFuture(null);
                });

        publisher.publishNewEvents();

        assertThat(outboxEvent.getStatus()).isEqualTo(OutboxEventStatus.PUBLISHED);
        assertThat(outboxEvent.getAttemptCount()).isZero();
        assertThat(outboxEvent.getPublishedAt()).isEqualTo(NOW);
        assertThat(outboxEvent.getNextAttemptAt()).isNull();
        assertThat(outboxEvent.getErrorMessage()).isNull();
    }

    @Test
    void shouldMarkClaimedEventFailedAndRetryableOnKafkaException() {
        OutboxEvent outboxEvent = outboxEvent();
        setId(outboxEvent, 2L);
        CompletableFuture<SendResult<String, OrderLifecycleEvent>> failedSend = new CompletableFuture<>();
        failedSend.completeExceptionally(new RuntimeException("Kafka temporarily unavailable"));

        when(outboxEventRepository.findRetryableEvents(
                eq(RETRYABLE_STATUSES),
                eq(NOW),
                eq(5),
                any(Pageable.class)
        )).thenReturn(List.of(outboxEvent));
        when(outboxEventRepository.findById(2L)).thenReturn(Optional.of(outboxEvent));
        when(kafkaTemplate.send(eq(TOPIC), eq("99"), any(OrderLifecycleEvent.class)))
                .thenAnswer(invocation -> {
                    assertThat(outboxEvent.getStatus()).isEqualTo(OutboxEventStatus.IN_PROGRESS);
                    assertThat(outboxEvent.getNextAttemptAt()).isEqualTo(NOW.plusSeconds(60));
                    return failedSend;
                });

        publisher.publishNewEvents();

        assertThat(outboxEvent.getStatus()).isEqualTo(OutboxEventStatus.FAILED);
        assertThat(outboxEvent.getAttemptCount()).isEqualTo(1);
        assertThat(outboxEvent.getNextAttemptAt()).isEqualTo(NOW.plusSeconds(30));
        assertThat(outboxEvent.getPublishedAt()).isNull();
        assertThat(outboxEvent.getErrorMessage()).isEqualTo("Kafka temporarily unavailable");
        verify(outboxEventRepository).findRetryableEvents(
                eq(RETRYABLE_STATUSES),
                eq(NOW),
                eq(5),
                any(Pageable.class)
        );
    }

    private OutboxEvent outboxEvent() {
        OrderLifecycleEvent event = orderLifecycleEvent();
        return OutboxEvent.forOrderLifecycleEvent(event, payloadJson(event));
    }

    private void setId(OutboxEvent outboxEvent, Long id) {
        ReflectionTestUtils.setField(outboxEvent, "id", id);
    }

    private OrderLifecycleEvent orderLifecycleEvent() {
        return new OrderLifecycleEvent(
                UUID.randomUUID(),
                99L,
                "correlation-1",
                NOW.minusSeconds(5),
                OrderLifecycleEventType.ORDER_CREATED,
                new OrderLifecyclePayload(
                        99L,
                        "CREATED",
                        null,
                        List.of(new OrderLinePayload(7L, 2))
                )
        );
    }

    private String payloadJson(OrderLifecycleEvent event) {
        try {
            return objectMapper.writeValueAsString(event);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException(e);
        }
    }
}
