package org.example.educheck.domain.attendance.dto.response;

import lombok.Builder;
import lombok.Getter;
import org.example.educheck.domain.studentCourseAttendance.entity.StudentCourseAttendanceV2;

import java.time.LocalDate;

@Getter
@Builder
public class MyAttendanceRecordResponseDto {

    private LocalDate lectureDate;
    private String attendanceStatus;
    private Long lectureSession;
    private String lectureTitle;

    public static MyAttendanceRecordResponseDto from(StudentCourseAttendanceV2 entity) {
        return MyAttendanceRecordResponseDto.builder()
                .lectureDate(entity.getLectureDate())
                .lectureTitle(entity.getLectureTitle())
                .attendanceStatus(entity.getAttendanceStatus())
                .lectureSession(entity.getLectureSession())
                .build();
    }

}
