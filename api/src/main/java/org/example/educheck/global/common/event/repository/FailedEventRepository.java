package org.example.educheck.global.common.event.repository;

import org.example.educheck.global.common.event.entity.FailedEvent;
import org.example.educheck.global.common.event.entity.Status;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FailedEventRepository extends JpaRepository<FailedEvent, Long> {
    List<FailedEvent> findByStatus(Status status);
}
