package org.example.educheck.domain.attendanceRegister.dto.response.today;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

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
        Map<String, Long> countMap = statuses.stream()
                .filter(status -> !status.getAttendanceStatus().equals("EXCUSED") && !status.getAttendanceStatus().equals("UPCOMING"))
                .collect(Collectors.groupingBy(
                        TodayLectureAttendanceStatus::getAttendanceStatus,
                        Collectors.counting()
                ));

        return new TodayLectureAttendanceSummary(
                countMap.getOrDefault("ATTENDANCE", 0L),
                countMap.getOrDefault("EARLY_LEAVE", 0L),
                countMap.getOrDefault("LATE", 0L),
                countMap.getOrDefault("ABSENT", 0L)
        );
    }

}
