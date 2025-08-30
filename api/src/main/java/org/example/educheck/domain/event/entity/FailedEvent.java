package org.example.educheck.domain.event.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import java.time.LocalDateTime;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FailedEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String eventTYPE;

    @Lob
    @Column(nullable = false)
    private String payload;

    @Lob
    private String errorMessage;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status;

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    @Builder
    public FailedEvent (String eventTYPE, String payload, String errorMessage) {
        this.eventTYPE = eventTYPE;
        this.payload = payload;
        this.errorMessage = errorMessage;
        this.status = Status.UNRESOLVED;
    }

    public void updateStatus(Status status) {
        this.status = status;
    }
}

