package org.example.educheck.global.rabbitmq.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.educheck.global.common.entity.BaseTimeEntity;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DeadLetterMessage extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String originalQueue;

    private String reason;

    @Lob
    @Column(nullable = false, columnDefinition = "TEXT")
    private String messageBody;

    private String sourceTable;

    private Long sourcePk;

    @Builder
    public DeadLetterMessage(String originalQueue, String messageBody, String reason, String sourceTable, Long sourcePk) {
        this.originalQueue = originalQueue;
        this.messageBody = messageBody;
        this.reason = reason;
        this.sourceTable = sourceTable;
        this.sourcePk = sourcePk;
    }

    public static DeadLetterMessage create(String originalQueue, String messageBody, String reason, String sourceTable, Long sourcePk) {
        return new DeadLetterMessage(originalQueue, messageBody, reason, sourceTable, sourcePk);
    }

}
