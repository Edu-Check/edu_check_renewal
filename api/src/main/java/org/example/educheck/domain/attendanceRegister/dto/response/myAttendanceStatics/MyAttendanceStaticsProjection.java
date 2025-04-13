package org.example.educheck.domain.attendanceRegister.dto.response.myAttendanceStatics;

public interface MyAttendanceStaticsProjection {
    Double getTotalAttendanceRate();
    Integer getLateCountUntilToday();
    Integer getEarlyLateCountUntilToday();
    Integer getAbsenceCountUntilToday();
    Integer getAdjustedAbsenceCount();
}
