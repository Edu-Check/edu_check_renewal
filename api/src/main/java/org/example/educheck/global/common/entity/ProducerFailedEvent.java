package org.example.educheck.global.common.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Table(name = "producer_failed_events",
        indexes = {
                @Index(name = "idx_status_created", columnList = "status, created_at"),
                @Index(name = "idx_entity_type_id", columnList = "entity_type, entity_id")
        })@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class ProducerFailedEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String entityType;

    @Column(nullable = false)
    private String entityId;

    @Column(nullable = false)
    private String targetExchange;

    @Column(nullable = false)
    private String targetRoutingKey;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String payload; // JSON

    @Column(columnDefinition = "TEXT")
    private String errorMessage;

    @Column(nullable = false)
    @Builder.Default
    private Integer retryCount = 0;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private FailStatus status; // PENDING, RETRYING, RESOLVED, FAILED

    @Column(nullable = false)
    private LocalDateTime createdAt;

    private LocalDateTime lastRetryAt;

    private LocalDateTime resolvedAt;

    public void incrementRetryCount() {
        this.retryCount++;
        this.lastRetryAt = LocalDateTime.now();
    }

    public void markResolved() {
        this.status = FailStatus.RESOLVED;
        this.resolvedAt = LocalDateTime.now();
    }

    public void markFailed() {
        this.status = FailStatus.FAILED;
    }
}
