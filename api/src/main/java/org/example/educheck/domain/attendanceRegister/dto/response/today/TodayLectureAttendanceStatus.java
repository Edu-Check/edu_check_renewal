package org.example.educheck.domain.attendanceRegister.dto.response.today;

import lombok.Builder;
import lombok.Getter;
import org.example.educheck.domain.attendanceRegister.entity.AttendanceRegister;

@Getter
@Builder
public class TodayLectureAttendanceStatus {

    private final Long memberId;
    private final String studentName;
    private final String studentPhoneNumber;
    private String attendanceStatus;

    public static TodayLectureAttendanceStatus from (AttendanceRegister attendanceRegister) {
        return TodayLectureAttendanceStatus.builder()
                .memberId(attendanceRegister.getMemberId())
                .studentName(attendanceRegister.getStudentName())
                .studentPhoneNumber(attendanceRegister.getStudentPhoneNumber())
                .attendanceStatus(attendanceRegister.getAttendanceStatus())
                .build();
    }
}
