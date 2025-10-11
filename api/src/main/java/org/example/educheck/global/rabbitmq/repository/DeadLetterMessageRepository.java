package org.example.educheck.global.rabbitmq.repository;

import org.example.educheck.global.rabbitmq.entity.DeadLetterMessage;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DeadLetterMessageRepository extends JpaRepository<DeadLetterMessage, Long> {
}
