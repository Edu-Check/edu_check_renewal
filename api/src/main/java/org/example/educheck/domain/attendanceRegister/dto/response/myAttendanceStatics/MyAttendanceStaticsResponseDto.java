package org.example.educheck.domain.attendanceRegister.dto.response.myAttendanceStatics;

import lombok.Builder;
import lombok.Getter;
import org.example.educheck.domain.course.entity.Course;

import java.time.LocalDate;

@Getter
@Builder
public class MyAttendanceStaticsResponseDto {

    private final double attendanceRate;
    private final int lateCount;
    private final int earlyLeaveCount;
    private final int absenceCount;
    private final int adjustedAbsenceCount;
    private final LocalDate startDate;
    private final LocalDate endDate;

    public static MyAttendanceStaticsResponseDto from(MyAttendanceStaticsProjection projection, Course course) {
        return MyAttendanceStaticsResponseDto.builder()
                .attendanceRate(projection.getTotalAttendanceRate())
                .lateCount(projection.getLateCountUntilToday())
                .earlyLeaveCount(projection.getEarlyLateCountUntilToday())
                .absenceCount(projection.getAbsenceCountUntilToday())
                .adjustedAbsenceCount(projection.getAdjustedAbsenceCount())
                .startDate(course.getStartDate())
                .endDate(course.getEndDate())
                .build();
    }

}
