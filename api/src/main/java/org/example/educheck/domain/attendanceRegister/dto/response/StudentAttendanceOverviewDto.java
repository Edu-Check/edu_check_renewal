package org.example.educheck.domain.attendanceRegister.dto.response;

import lombok.Builder;
import lombok.Getter;
import org.example.educheck.domain.member.entity.Member;
import org.example.educheck.domain.member.student.entity.Student;
import org.springframework.data.domain.Page;

@Builder
@Getter
public class StudentAttendanceOverviewDto {

    private Long studentId;
    private String studentName;
    private String studentPhoneNumber;
    private String studentEmail;
    private AttendanceRateProjection attendanceRate;
    private Page<AttendanceRecordResponseDto> attendanceRecordResponseDtos;

    public static StudentAttendanceOverviewDto from(Member member, AttendanceRateProjection attendanceRate, Page<AttendanceRecordResponseDto> attendanceRecordResponseDto) {
        return StudentAttendanceOverviewDto.builder()
                .studentId(member.getId())
                .studentName(member.getName())
                .studentPhoneNumber(member.getPhoneNumber())
                .studentEmail(member.getEmail())
                .attendanceRate(attendanceRate)
                .attendanceRecordResponseDtos(attendanceRecordResponseDto)
                .build();
    }
}
