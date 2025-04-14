package org.example.educheck.domain.attendanceRegister.dto.response.myAttendanceRecord;

import java.time.LocalDate;

public interface MyAttendanceRecordProjection {

    LocalDate getLectureDate();
    String getAttendanceStatus();

}
