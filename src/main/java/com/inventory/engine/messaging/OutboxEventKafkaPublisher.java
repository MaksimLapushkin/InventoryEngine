package com.inventory.engine.messaging;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.inventory.engine.model.OutboxEvent;
import com.inventory.engine.model.OutboxEventStatus;
import com.inventory.engine.repository.OutboxEventRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.domain.PageRequest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.SimpleTransactionStatus;
import org.springframework.transaction.support.TransactionOperations;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

@Component
@ConditionalOnProperty(prefix = "app.kafka.order-lifecycle", name = "enabled", havingValue = "true")
public class OutboxEventKafkaPublisher {

    private static final Logger log = LoggerFactory.getLogger(OutboxEventKafkaPublisher.class);

    private static final int MAX_ERROR_MESSAGE_LENGTH = 1000;
    private static final Duration INITIAL_RETRY_BACKOFF = Duration.ofSeconds(30);
    private static final Duration MAX_RETRY_BACKOFF = Duration.ofMinutes(5);
    private static final List<OutboxEventStatus> RETRYABLE_STATUSES = List.of(
            OutboxEventStatus.NEW,
            OutboxEventStatus.IN_PROGRESS,
            OutboxEventStatus.FAILED
    );

    private final OutboxEventRepository outboxEventRepository;
    private final KafkaTemplate<String, OrderLifecycleEvent> kafkaTemplate;
    private final ObjectMapper objectMapper;
    private final TransactionOperations transactionOperations;
    private final String topic;
    private final int batchSize;
    private final int maxAttempts;
    private final Duration claimTimeout;
    private final Clock clock;

    @Autowired
    public OutboxEventKafkaPublisher(
            OutboxEventRepository outboxEventRepository,
            KafkaTemplate<String, OrderLifecycleEvent> kafkaTemplate,
            ObjectMapper objectMapper,
            PlatformTransactionManager transactionManager,
            @Value("${app.kafka.order-lifecycle.topic}") String topic,
            @Value("${app.outbox.publisher.batch-size:50}") int batchSize,
            @Value("${app.outbox.publisher.max-attempts:5}") int maxAttempts,
            @Value("${app.outbox.publisher.claim-timeout-ms:60000}") long claimTimeoutMs
    ) {
        this(
                outboxEventRepository,
                kafkaTemplate,
                objectMapper,
                new TransactionTemplate(transactionManager),
                topic,
                batchSize,
                maxAttempts,
                Duration.ofMillis(Math.max(claimTimeoutMs, 1L)),
                Clock.systemUTC()
        );
    }

    OutboxEventKafkaPublisher(
            OutboxEventRepository outboxEventRepository,
            KafkaTemplate<String, OrderLifecycleEvent> kafkaTemplate,
            ObjectMapper objectMapper,
            String topic,
            int batchSize,
            int maxAttempts,
            Clock clock
    ) {
        this(
                outboxEventRepository,
                kafkaTemplate,
                objectMapper,
                immediateTransactionOperations(),
                topic,
                batchSize,
                maxAttempts,
                Duration.ofSeconds(60),
                clock
        );
    }

    OutboxEventKafkaPublisher(
            OutboxEventRepository outboxEventRepository,
            KafkaTemplate<String, OrderLifecycleEvent> kafkaTemplate,
            ObjectMapper objectMapper,
            TransactionOperations transactionOperations,
            String topic,
            int batchSize,
            int maxAttempts,
            Duration claimTimeout,
            Clock clock
    ) {
        this.outboxEventRepository = outboxEventRepository;
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
        this.transactionOperations = transactionOperations;
        this.topic = topic;
        this.batchSize = Math.max(batchSize, 1);
        this.maxAttempts = Math.max(maxAttempts, 1);
        this.claimTimeout = claimTimeout.isNegative() || claimTimeout.isZero()
                ? Duration.ofSeconds(60)
                : claimTimeout;
        this.clock = clock;
    }

    @Scheduled(fixedDelayString = "${app.outbox.publisher.fixed-delay-ms:5000}")
    public void publishNewEvents() {
        List<OutboxEvent> events = claimRetryableEvents();

        if (!events.isEmpty()) {
            log.info("Claimed {} outbox event(s) for publishing", events.size());
        }

        for (OutboxEvent event : events) {
            publishClaimedEvent(event);
        }
    }

    private List<OutboxEvent> claimRetryableEvents() {
        return transactionOperations.execute(status -> {
            Instant now = clock.instant();
            Instant claimExpiresAt = now.plus(claimTimeout);

            List<OutboxEvent> events = outboxEventRepository.findRetryableEvents(
                    RETRYABLE_STATUSES,
                    now,
                    maxAttempts,
                    PageRequest.of(0, batchSize)
            );

            for (OutboxEvent event : events) {
                event.markInProgress(claimExpiresAt);
            }

            return List.copyOf(events);
        });
    }

    private void publishClaimedEvent(OutboxEvent outboxEvent) {
        try {
            OrderLifecycleEvent event = deserialize(outboxEvent);

            log.info(
                    "Publishing outbox event: id={}, aggregateId={}, eventType={}, attemptCount={}",
                    outboxEvent.getEventId(),
                    outboxEvent.getAggregateId(),
                    outboxEvent.getEventType(),
                    outboxEvent.getAttemptCount()
            );

            kafkaTemplate.send(topic, outboxEvent.getAggregateId(), event).get();

            log.info("Kafka ACK received for outbox event: {}", outboxEvent.getEventId());

            markPublished(outboxEvent);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Interrupted while publishing outbox event {}", outboxEvent.getEventId(), e);
            markFailed(outboxEvent, e);
        } catch (ExecutionException | RuntimeException e) {
            log.error("Failed to publish outbox event {}", outboxEvent.getEventId(), e);
            markFailed(outboxEvent, e);
        }
    }

    private void markPublished(OutboxEvent outboxEvent) {
        transactionOperations.execute(status -> {
            findCurrentClaim(outboxEvent).ifPresentOrElse(
                    event -> {
                        event.markPublished(clock.instant());
                        log.info("Marked outbox event as PUBLISHED: {}", outboxEvent.getEventId());
                    },
                    () -> log.warn(
                            "Could not mark outbox event as PUBLISHED because current claim was not found: {}",
                            outboxEvent.getEventId()
                    )
            );
            return null;
        });
    }

    private void markFailed(OutboxEvent outboxEvent, Exception exception) {
        transactionOperations.execute(status -> {
            findCurrentClaim(outboxEvent).ifPresentOrElse(
                    event -> {
                        int nextAttemptCount = event.getAttemptCount() + 1;
                        Instant nextAttemptAt = nextAttemptCount >= maxAttempts
                                ? null
                                : clock.instant().plus(backoffForAttempt(nextAttemptCount));

                        event.markFailed(errorMessage(exception), nextAttemptAt);

                        log.warn(
                                "Marked outbox event as FAILED: id={}, nextAttemptCount={}, nextAttemptAt={}",
                                outboxEvent.getEventId(),
                                nextAttemptCount,
                                nextAttemptAt
                        );
                    },
                    () -> log.warn(
                            "Could not mark outbox event as FAILED because current claim was not found: {}",
                            outboxEvent.getEventId()
                    )
            );
            return null;
        });
    }

    private Optional<OutboxEvent> findCurrentClaim(OutboxEvent claimedEvent) {
        return outboxEventRepository.findById(claimedEvent.getId())
                .filter(event -> event.isInProgressClaim(claimedEvent.getNextAttemptAt()));
    }

    private Duration backoffForAttempt(int attemptCount) {
        int exponent = Math.min(attemptCount - 1, 4);
        Duration backoff = INITIAL_RETRY_BACKOFF.multipliedBy(1L << exponent);
        if (backoff.compareTo(MAX_RETRY_BACKOFF) > 0) {
            return MAX_RETRY_BACKOFF;
        }
        return backoff;
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

    private static TransactionOperations immediateTransactionOperations() {
        return new TransactionOperations() {
            @Override
            public <T> T execute(TransactionCallback<T> action) {
                return action.doInTransaction(new SimpleTransactionStatus());
            }
        };
    }
}