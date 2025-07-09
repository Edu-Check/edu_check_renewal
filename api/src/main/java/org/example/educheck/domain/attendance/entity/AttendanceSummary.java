package org.example.educheck.domain.attendance.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@IdClass(AttendanceSummaryId.class)
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AttendanceSummary {

    @Id
    private Long studentId;

    @Id
    private Long courseId;

    private Double totalAttendanceRate;

    private Integer lateCountUntilToday;

    @Column(name = "early_leave_count_until_today")
    private Integer earlyLeaveCountUntilToday;

    @Column(name = "absence_count_until_today")
    private Integer absenceCountUntilToday;

    @Column(name = "adjusted_absence_count")
    private Integer adjustedAbsenceCount;

    @Column(name = "attendance_rate_until_today")
    private Double attendanceRateUntilToday;

    @Column(name = "course_progress_rate")
    private Double courseProgressRate;


    @Column(name = "lecture_count_until_today")
    private Integer lectureCountUntilToday;

    @Column(name = "attendance_count_until_today")
    private Integer attendanceCountUntilToday;

    @Column(name = "adjusted_absent_by_late_or_early_leave")
    private Integer adjustedAbsentByLateOrEarlyLeave;

    @Column(name = "total_lecture_count")
    private Integer totalLectureCount;

    public void updateSummary(AttendanceStatus oldStatus, AttendanceStatus newStatus) {
        if (oldStatus != null) {
            switch (oldStatus) {
                case AttendanceStatus.ATTENDANCE:
                    this.attendanceCountUntilToday--;
                    break;
                case AttendanceStatus.LATE:
                    this.lateCountUntilToday--;
                    break;
                case AttendanceStatus.EARLY_LEAVE:
                    this.earlyLeaveCountUntilToday--;
                    break;
                case AttendanceStatus.ABSENCE:
                    this.absenceCountUntilToday--;
                    break;
            }
        }

        if (newStatus != null) {
            switch (newStatus) {
                case AttendanceStatus.ATTENDANCE:
                    this.attendanceCountUntilToday++;
                    break;
                case AttendanceStatus.LATE:
                    this.lateCountUntilToday++;
                    break;
                case AttendanceStatus.EARLY_LEAVE:
                    this.earlyLeaveCountUntilToday++;
                    break;
                case AttendanceStatus.ABSENCE:
                    this.absenceCountUntilToday++;
                    break;
            }
        }

        this.adjustedAbsentByLateOrEarlyLeave = (this.lateCountUntilToday + this.earlyLeaveCountUntilToday) / 3;

        if (this.lectureCountUntilToday > 0) {
            this.attendanceRateUntilToday = ((double) (this.attendanceCountUntilToday + this.lateCountUntilToday + this.earlyLeaveCountUntilToday - this.adjustedAbsentByLateOrEarlyLeave) / this.lectureCountUntilToday) * 100.0;
        } else {
            this.attendanceRateUntilToday = null;
        }

        if (this.totalLectureCount > 0) {
            this.totalAttendanceRate = ((double) (this.attendanceCountUntilToday + this.lateCountUntilToday + this.earlyLeaveCountUntilToday - this.adjustedAbsentByLateOrEarlyLeave) / this.totalLectureCount) * 100.0;
            this.courseProgressRate = ((double) this.lectureCountUntilToday / this.totalLectureCount) * 100.0;
        } else {
            this.totalAttendanceRate = null;
            this.courseProgressRate = null;
        }

        this.adjustedAbsenceCount = this.adjustedAbsentByLateOrEarlyLeave + this.absenceCountUntilToday;
    }
}