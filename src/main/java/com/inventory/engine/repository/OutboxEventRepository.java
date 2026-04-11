package com.inventory.engine.repository;

import com.inventory.engine.model.OutboxEvent;
import com.inventory.engine.model.OutboxEventStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;

public interface OutboxEventRepository extends JpaRepository<OutboxEvent, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            select event
            from OutboxEvent event
            where event.status in :statuses
              and event.attemptCount < :maxAttempts
              and (event.nextAttemptAt is null or event.nextAttemptAt <= :now)
            order by event.createdAt asc
            """)
    List<OutboxEvent> findRetryableEvents(
            @Param("statuses") List<OutboxEventStatus> statuses,
            @Param("now") Instant now,
            @Param("maxAttempts") int maxAttempts,
            Pageable pageable
    );
}
