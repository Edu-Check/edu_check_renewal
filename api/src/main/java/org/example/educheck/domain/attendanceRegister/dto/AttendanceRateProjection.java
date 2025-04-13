package org.example.educheck.domain.attendanceRegister.dto;

public interface AttendanceRateProjection {

    Long getStudentId();
    Double getAttendanceRateUntilToday();
    Double getTotalAttendanceRate();
    Double getCourseProgressRate();

}
