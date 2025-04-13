package org.example.educheck.domain.attendanceRegister.dto.response.adminStudentDetail;

import java.time.LocalDate;

public interface AttendanceRecordResponseDto {

     LocalDate getLectureDate();
     int getLectureSession();
     String getLectureTitle();
     String getAttendanceStatus();
}
