package org.example.educheck.domain.attendance.dto.response;

import lombok.Builder;
import lombok.Getter;
import org.example.educheck.domain.attendance.entity.Attendance;
import org.example.educheck.domain.attendance.entity.AttendanceSummary;
import org.example.educheck.domain.course.entity.Course;

import java.time.LocalDate;

@Getter
@Builder
public class MyAttendanceStaticsResponseDto {

    private final double attendanceRate;
    private final double courseProgressRate;
    private final int lateCount;
    private final int earlyLeaveCount;
    private final int absenceCount;
    private final int adjustedAbsenceCount;
    private final LocalDate startDate;
    private final LocalDate endDate;

    public static MyAttendanceStaticsResponseDto from(AttendanceSummary summary, Course course) {
        return MyAttendanceStaticsResponseDto.builder()
                .attendanceRate(summary.getTotalAttendanceRate() != null ? summary.getTotalAttendanceRate() : 0.0)
                .courseProgressRate(summary.getCourseProgressRate() != null ?summary.getCourseProgressRate() : 0.0)
                .lateCount(summary.getLateCountUntilToday() != null ? summary.getLateCountUntilToday() : 0)
                .earlyLeaveCount(summary.getEarlyLeaveCountUntilToday() != null ? summary.getEarlyLeaveCountUntilToday() : 0)
                .absenceCount(summary.getAbsenceCountUntilToday() != null ? summary.getAbsenceCountUntilToday() : 0)
                .adjustedAbsenceCount(summary.getAdjustedAbsenceCount() != null ? summary.getAdjustedAbsenceCount() : 0)
                .startDate(course.getStartDate())
                .endDate(course.getEndDate())
                .build();
    }
}
