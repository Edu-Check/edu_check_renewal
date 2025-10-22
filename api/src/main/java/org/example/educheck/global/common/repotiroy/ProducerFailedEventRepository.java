package org.example.educheck.global.common.repotiroy;

import org.example.educheck.global.common.entity.ProducerFailedEvent;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProducerFailedEventRepository extends JpaRepository<ProducerFailedEvent, Integer> {
}
