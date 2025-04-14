package org.example.educheck.domain.attendanceRegister.dto.response.myAttendanceRecord;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class MyAttendanceRecordResponseDto {

    private Long studentId;
    private Long courseId;
    private List<MyAttendanceRecordProjection> records;

    public static MyAttendanceRecordResponseDto from(Long studentId, Long courseId, List<MyAttendanceRecordProjection> records) {
        return MyAttendanceRecordResponseDto.builder()
                .studentId(studentId)
                .courseId(courseId)
                .records(records)
                .build();
    }
}
