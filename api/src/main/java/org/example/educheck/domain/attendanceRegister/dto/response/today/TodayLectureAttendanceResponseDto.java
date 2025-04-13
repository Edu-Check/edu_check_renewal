package org.example.educheck.domain.attendanceRegister.dto.response.today;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class TodayLectureAttendanceResponseDto {

    private final Long memberId;
    private final TodayLectureAttendanceSummary summary;
    private final List<TodayLectureAttendanceStatus> records;

    public static TodayLectureAttendanceResponseDto from(Long memberId, List<TodayLectureAttendanceStatus> attendanceStatusList) {
        return TodayLectureAttendanceResponseDto.builder()
                .memberId(memberId)
                .records(attendanceStatusList)
                .summary(TodayLectureAttendanceSummary.from(attendanceStatusList))
                .build();
    }

}
