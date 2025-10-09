package org.example.educheck.domain.notice.repository;

import org.example.educheck.domain.notice.entity.NoticeReceipt;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NoticeReceiptRepository extends JpaRepository<NoticeReceipt, Long> {
}
