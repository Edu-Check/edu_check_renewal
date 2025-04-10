package org.example.educheck.domain.attendanceRegister.dto.response.today;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.example.educheck.domain.attendance.entity.AttendanceStatus;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 집계시, 유고 결석 승인으로 인한 출석과 수업 시작 전 상태는 집계하지 않는다.
 */
@Getter
@Builder
@AllArgsConstructor
public class TodayLectureAttendanceSummary {

    private final Long totalAttendance;
    private final Long totalEarlyLeave;
    private final Long totalLate;
    private final Long totalAbsence;

    public static TodayLectureAttendanceSummary from(List<TodayLectureAttendanceStatus> statuses) {
        Map<AttendanceStatus, Long> countMap = statuses.stream()
                .filter(status ->
                        status.getAttendanceStatus() !=AttendanceStatus.EXCUSED &&
                        status.getAttendanceStatus() != AttendanceStatus.UPCOMING)
                .collect(Collectors.groupingBy(
                        TodayLectureAttendanceStatus::getAttendanceStatus,
                        Collectors.counting()
                ));

        return new TodayLectureAttendanceSummary(
                countMap.getOrDefault(AttendanceStatus.ATTENDANCE, 0L),
                countMap.getOrDefault(AttendanceStatus.EARLY_LEAVE, 0L),
                countMap.getOrDefault(AttendanceStatus.LATE, 0L),
                countMap.getOrDefault(AttendanceStatus.ABSENCE, 0L)
        );
    }

}
