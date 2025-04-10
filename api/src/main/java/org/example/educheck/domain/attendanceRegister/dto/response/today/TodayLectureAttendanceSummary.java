package org.example.educheck.domain.attendanceRegister.dto.response.today;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.example.educheck.domain.attendance.entity.AttendanceStatus;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Getter
@Builder
@AllArgsConstructor
public class TodayLectureAttendanceSummary {

    private final Long totalAttendance;
    private final Long totalEarlyLeave;
    private final Long totalLate;
    private final Long totalAbsence;
    private final Long totalExcused;
    private final Long totalUpcoming;

    public static TodayLectureAttendanceSummary from(List<TodayLectureAttendanceStatus> statuses) {
        Map<AttendanceStatus, Long> countMap = statuses.stream()
                .collect(Collectors.groupingBy(
                        TodayLectureAttendanceStatus::getAttendanceStatus,
                        Collectors.counting()
                ));

        return new TodayLectureAttendanceSummary(
                countMap.getOrDefault(AttendanceStatus.ATTENDANCE, 0L),
                countMap.getOrDefault(AttendanceStatus.EARLY_LEAVE, 0L),
                countMap.getOrDefault(AttendanceStatus.LATE, 0L),
                countMap.getOrDefault(AttendanceStatus.ABSENCE, 0L),
                countMap.getOrDefault(AttendanceStatus.EXCUSED, 0L),
                countMap.getOrDefault(AttendanceStatus.UPCOMING, 0L)
        );
    }

}
