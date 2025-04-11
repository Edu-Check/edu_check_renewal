package org.example.educheck.domain.attendanceRegister.dto.response;


import lombok.Builder;
import lombok.Getter;
import org.example.educheck.domain.studentCourseAttendance.entity.StudentCourseAttendanceV2;

import java.time.LocalDate;

@Getter
@Builder
public class AttendanceRecordResponseDto {

    private LocalDate lectureDateTime;
    private String attendanceStatus;
    private Long lectureSession;

    public static AttendanceRecordResponseDto from(StudentCourseAttendanceV2 entity) {
        return AttendanceRecordResponseDto.builder()
                .lectureDateTime(entity.getLectureDate())
                .attendanceStatus(entity.getAttendanceStatus())
                .lectureSession(entity.getLectureSession())
                .build();
    }
}
