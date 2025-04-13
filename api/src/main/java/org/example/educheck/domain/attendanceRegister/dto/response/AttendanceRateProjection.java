package org.example.educheck.domain.attendanceRegister.dto.response;

public interface AttendanceRateProjection {

    Double getAttendanceRateUntilToday();
    Double getTotalAttendanceRate();
    Double getCourseProgressRate();

}
