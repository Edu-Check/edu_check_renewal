package org.example.educheck.domain.event.repository;

import jdk.jshell.Snippet;
import org.example.educheck.domain.event.entity.FailedEvent;
import org.example.educheck.domain.event.entity.Status;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FailedEventRepository extends JpaRepository<FailedEvent, Long> {
    List<FailedEvent> findByStatus(Status status);
}
