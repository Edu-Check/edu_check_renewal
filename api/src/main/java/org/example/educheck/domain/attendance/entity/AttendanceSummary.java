package org.example.educheck.domain.attendance.entity;

import jakarta.persistence.*;
import lombok.*;
import org.example.educheck.domain.absenceattendance.entity.AbsenceAttendance;
import org.example.educheck.domain.lecture.entity.Lecture;
import org.example.educheck.global.common.time.SystemTimeProvider;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Entity
@IdClass(AttendanceSummaryId.class)
@Getter
@Setter
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

    @Setter
    @Column(name = "lecture_count_until_today")
    private Integer lectureCountUntilToday;

    @Column(name = "attendance_count_until_today")
    private Integer attendanceCountUntilToday;

    @Column(name = "adjusted_absent_by_late_or_early_leave")
    private Integer adjustedAbsentByLateOrEarlyLeave;

    @Setter
    @Column(name = "total_lecture_count")
    private Integer totalLectureCount;

    private static final int LATE_OR_EARLY_LEAVE_COUNT_FOR_ABSENCE = 3;

    public void updateSummary(AttendanceStatus oldStatus, AttendanceStatus newStatus) {
        if (oldStatus != null) {
        if (oldStatus != null) {
            switch (oldStatus) {
                case ATTENDANCE:
                    this.attendanceCountUntilToday--;
                    break;
                case LATE:
                    this.lateCountUntilToday--;
                    break;
                case EARLY_LEAVE:
                    this.earlyLeaveCountUntilToday--;
                    break;
                case ABSENCE:
                    this.absenceCountUntilToday--;
                    break;
            }
        }
                case ATTENDANCE:
                    this.attendanceCountUntilToday--;
                    break;
                case LATE:
                    this.lateCountUntilToday--;
                    break;
                case EARLY_LEAVE:
                    this.earlyLeaveCountUntilToday--;
                    break;
                case ABSENCE:
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

        this.adjustedAbsentByLateOrEarlyLeave = (this.lateCountUntilToday + this.earlyLeaveCountUntilToday) / LATE_OR_EARLY_LEAVE_COUNT_FOR_ABSENCE;

        if (this.lectureCountUntilToday > 0) {
        if (this.lectureCountUntilToday > 0) {
            int effectiveAttendance = this.attendanceCountUntilToday
                                    + this.lateCountUntilToday
                                    + this.earlyLeaveCountUntilToday
                                    - this.adjustedAbsentByLateOrEarlyLeave;
            this.attendanceRateUntilToday = ((double) effectiveAttendance
                                            / this.lectureCountUntilToday) * 100.0;
        } else {
            this.attendanceRateUntilToday = 0.0;
        }

        if (this.totalLectureCount > 0) {
            this.totalAttendanceRate = ((double) (this.attendanceCountUntilToday
                    + this.lateCountUntilToday
                    + this.earlyLeaveCountUntilToday
                    - this.adjustedAbsentByLateOrEarlyLeave)
                    / this.totalLectureCount) * 100.0;
            this.courseProgressRate = ((double) this.lectureCountUntilToday
                    / this.totalLectureCount) * 100.0;
        } else {
            this.totalAttendanceRate = 0.0;
            this.courseProgressRate = 0.0;
        }

        this.adjustedAbsenceCount = this.adjustedAbsentByLateOrEarlyLeave + this.absenceCountUntilToday;
    }

    @Override
    public String toString() {
        return "AttendanceSummary{" +
                "studentId=" + studentId +
                ", courseId=" + courseId +
                ", totalAttendanceRate=" + totalAttendanceRate +
                ", lateCountUntilToday=" + lateCountUntilToday +
                ", earlyLeaveCountUntilToday=" + earlyLeaveCountUntilToday +
                ", absenceCountUntilToday=" + absenceCountUntilToday +
                ", adjustedAbsenceCount=" + adjustedAbsenceCount +
                ", attendanceRateUntilToday=" + attendanceRateUntilToday +
                ", courseProgressRate=" + courseProgressRate +
                ", lectureCountUntilToday=" + lectureCountUntilToday +
                ", attendanceCountUntilToday=" + attendanceCountUntilToday +
                ", adjustedAbsentByLateOrEarlyLeave=" + adjustedAbsentByLateOrEarlyLeave +
                ", totalLectureCount=" + totalLectureCount +
                '}';
    }

    public void recalculateWithApprovedAbsences(List<Attendance> attendances, List<AbsenceAttendance> approvedAbsences, List<Lecture> allLectures, SystemTimeProvider timeProvider) {
        this.attendanceCountUntilToday = 0;
        this.lateCountUntilToday = 0;
        this.earlyLeaveCountUntilToday = 0;
        this.absenceCountUntilToday = 0;
        this.adjustedAbsentByLateOrEarlyLeave = 0;

        Map<LocalDate, Attendance> attendanceMap = attendances.stream()
                .collect(Collectors.toMap(att -> att.getLecture().getDate(), Function.identity()));

        Set<LocalDate> approvedAbsenceDates = approvedAbsences.stream()
                .flatMap(absence -> absence.getStartTime().datesUntil(absence.getEndTime().plusDays(1)))
                .collect(Collectors.toSet());

        LocalDate today = timeProvider.nowDate();
        List<Lecture> lecturesUntilToday = allLectures.stream()
                .filter(lecture -> !lecture.getDate().isAfter(today))
                .toList();

        this.lectureCountUntilToday = lecturesUntilToday.size();

        for (Lecture lecture : lecturesUntilToday) {
            LocalDate lectureDate = lecture.getDate();

            if (approvedAbsenceDates.contains(lectureDate)) {
                this.attendanceCountUntilToday++;
                continue;
            }

            Attendance attendanceRecord = attendanceMap.get(lectureDate);

            if (attendanceRecord != null) {
                switch (attendanceRecord.getAttendanceStatus()) {
                switch (attendanceRecord.getAttendanceStatus()) {
                    case ATTENDANCE:
                        this.attendanceCountUntilToday++;
                        break;
                    case LATE:
                        this.lateCountUntilToday++;
                        break;
                    case EARLY_LEAVE:
                        this.earlyLeaveCountUntilToday++;
                        break;
                }
            } else {
                // 출석 기록이 없다면 결석
                this.absenceCountUntilToday++;
            }
        }

        int totalLateAndEarlyLeave = this.lateCountUntilToday + this.earlyLeaveCountUntilToday;
        this.adjustedAbsentByLateOrEarlyLeave = totalLateAndEarlyLeave / LATE_OR_EARLY_LEAVE_COUNT_FOR_ABSENCE;

        int totalAbsence = this.absenceCountUntilToday + this.adjustedAbsentByLateOrEarlyLeave;
        int actualAttendanceCount = attendanceCountUntilToday;

        if (this.lectureCountUntilToday > 0) {
            int effectiveAttendance = actualAttendanceCount + this.lateCountUntilToday +
                                     this.earlyLeaveCountUntilToday - this.adjustedAbsentByLateOrEarlyLeave;
            this.attendanceRateUntilToday = ((double) effectiveAttendance / this.lectureCountUntilToday) * 100.0;
        } else {
            this.attendanceRateUntilToday = 0.0;
        }

        this.totalLectureCount = allLectures.size();
        if (this.totalLectureCount > 0) {
            // 과정 진행률
            this.courseProgressRate = ((double) this.lectureCountUntilToday / this.totalLectureCount) * 100;

            // 전체 출석률
            this.totalAttendanceRate = ((double) actualAttendanceCount / this.totalLectureCount) * 100;
        } else {
            this.courseProgressRate = 0.0;
            this.totalAttendanceRate = 0.0;
        }

        this.adjustedAbsenceCount = totalAbsence;

    }
}