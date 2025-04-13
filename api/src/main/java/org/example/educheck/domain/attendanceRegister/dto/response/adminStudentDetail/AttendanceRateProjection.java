package org.example.educheck.domain.attendanceRegister.dto.response.adminStudentDetail;

public interface AttendanceRateProjection {

    Double getAttendanceRateUntilToday();
    Double getTotalAttendanceRate();
    Double getCourseProgressRate();

}
