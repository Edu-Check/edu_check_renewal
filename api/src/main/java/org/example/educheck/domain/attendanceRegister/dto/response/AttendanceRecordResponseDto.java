package org.example.educheck.domain.attendanceRegister.dto.response;

import java.time.LocalDate;

public interface AttendanceRecordResponseDto {

     LocalDate getLectureDate();
     int getLectureSession();
     String getLectureTitle();
     String getAttendanceStatus();
}
