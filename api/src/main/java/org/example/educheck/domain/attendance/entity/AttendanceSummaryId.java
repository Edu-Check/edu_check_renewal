package org.example.educheck.domain.attendance.entity;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class AttendanceSummaryId implements Serializable {
    private Long studentId;
    private Long courseId;
}