package org.example.educheck.domain.attendanceRegister.dto.response.today;

import org.example.educheck.domain.absenceattendance.entity.AbsenceAttendance;
import org.example.educheck.domain.attendance.entity.AttendanceStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("출석 상태별 통계 요약 테스트")
class TodayLectureAttendanceSummaryTest {

    @Test
    void 출석상태리스트로_출석통계_집계_성공_테스트() {
        //given
        List<TodayLectureAttendanceStatus> status = List.of(
                makeStatus(1L, AttendanceStatus.ATTENDANCE),
                makeStatus(2L, AttendanceStatus.ATTENDANCE),
                makeStatus(3L, AttendanceStatus.LATE),
                makeStatus(4L, AttendanceStatus.EARLY_LEAVE),
                makeStatus(5L, AttendanceStatus.ABSENCE),
                makeStatus(6L, AttendanceStatus.EXCUSED),
                makeStatus(7L, AttendanceStatus.UPCOMING)
        );

        //when
        TodayLectureAttendanceSummary summary = TodayLectureAttendanceSummary.from(status);

        //then
        assertEquals(2L, summary.getTotalAttendance());
        assertEquals(1L, summary.getTotalLate());
        assertEquals(1L, summary.getTotalEarlyLeave());
        assertEquals(1L, summary.getTotalAbsence());
        assertEquals(1L, summary.getTotalExcused());
        assertEquals(1L, summary.getTotalUpcoming());
    }

    private TodayLectureAttendanceStatus makeStatus(Long memberId, AttendanceStatus status) {
        return TodayLectureAttendanceStatus.builder()
                .memberId(memberId)
                .studentName("학생" + memberId)
                .studentPhoneNumber("010-1111-111"+memberId)
                .attendanceStatus(status)
                .build();
    }

}