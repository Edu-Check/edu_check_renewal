package org.example.educheck.domain.attendance.entity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;


class AttendanceSummaryTest {

    private AttendanceSummary summary;

    @DisplayName("총 20번의 강의가 있고, 오늘까지 10번의 강의가 진행되었다고 가정한다.")
    @BeforeEach
    void setUp() {
        summary = AttendanceSummary.builder()
                .studentId(1L)
                .courseId(1L)
                .totalAttendanceRate(0.0)
                .lateCountUntilToday(0)
                .earlyLeaveCountUntilToday(0)
                .absenceCountUntilToday(0)
                .adjustedAbsenceCount(0)
                .attendanceRateUntilToday(0.0)
                .courseProgressRate(0.0)
                .lectureCountUntilToday(10)
                .attendanceCountUntilToday(0)
                .adjustedAbsentByLateOrEarlyLeave(0)
                .totalLectureCount(20)
                .build();
    }

    @Test
    @DisplayName("첫 출석을 'ATTENDANCE'로 기록한다.")
    void updateSummary_InitialAttendance() {
        //when
        summary.updateSummary(null, AttendanceStatus.ATTENDANCE);

        //then
        assertThat(summary.getAttendanceCountUntilToday()).isEqualTo(1);
        assertThat(summary.getLateCountUntilToday()).isZero();
        assertThat(summary.getEarlyLeaveCountUntilToday()).isZero();
        assertThat(summary.getAbsenceCountUntilToday()).isZero();
    }

    @Test
    @DisplayName("출석 -> 지각으로 변경한다")
    void updateSummary_FromAttendanceToLate() {
        //given
        summary.updateSummary(null, AttendanceStatus.ATTENDANCE);
        assertThat(summary.getAttendanceCountUntilToday()).isEqualTo(1);

        //when
        summary.updateSummary(AttendanceStatus.ATTENDANCE, AttendanceStatus.LATE);

        //then
        assertThat(summary.getAttendanceCountUntilToday()).isZero();
        assertThat(summary.getLateCountUntilToday()).isEqualTo(1);
    }

    @Test
    @DisplayName("지각 3회는 결석 1회다")
    void updateSummary_지각3회는_결석1회_정책() {
        //given
        summary.updateSummary(null, AttendanceStatus.LATE);
        summary.updateSummary(null, AttendanceStatus.LATE);
        summary.updateSummary(null, AttendanceStatus.LATE);

        //then
        assertThat(summary.getLateCountUntilToday()).isEqualTo(3);
        assertThat(summary.getAdjustedAbsentByLateOrEarlyLeave()).isEqualTo(1);
        assertThat(summary.getAdjustedAbsenceCount()).isEqualTo(1);

    }

    @Test
    @DisplayName("출석률 확인")
    void updateSummary_출석률() {
        //given
        summary.setAttendanceCountUntilToday(5);
        summary.setLateCountUntilToday(2);
        summary.setEarlyLeaveCountUntilToday(1);
        summary.setAbsenceCountUntilToday(0);

        //출석5, 지각3, 조퇴2 -> 조정된 결석 1, 조정된 출석7 -> 7/10 -> 70

        //when
        summary.updateSummary(AttendanceStatus.ATTENDANCE, AttendanceStatus.LATE);

        //that
        assertThat(summary.getAttendanceRateUntilToday()).isEqualTo(70.0);

    }

}