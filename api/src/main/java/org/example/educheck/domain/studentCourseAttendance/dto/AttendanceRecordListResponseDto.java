package org.example.educheck.domain.studentCourseAttendance.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class AttendanceRecordListResponseDto {

    private Long requesterId;
    private String requesterName;
    private Long studentId;
    private String studentName;
    private String studentPhoneNumber;
    private Long courseId;
    private String courseName;
    private AttendanceRateStatisticsDto statistics;
    private List<AttendanceRecordResponseDto> attendanceRecordList;

    private long totalPages;
    private boolean hasNext;
    private boolean hasPrevious;


}
