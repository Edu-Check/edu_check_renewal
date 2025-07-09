package org.example.educheck.domain.attendance.repository;

import org.example.educheck.domain.attendance.entity.AttendanceSummary;
import org.example.educheck.domain.attendance.entity.AttendanceSummaryId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AttendanceSummaryRepository extends JpaRepository<AttendanceSummary, AttendanceSummaryId> {
}