package org.example.educheck.domain.absenceattendance.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.educheck.domain.absenceattendance.entity.AbsenceAttendance;
import org.example.educheck.domain.absenceattendanceattachmentfile.dto.response.AttachmentFileResponseDto;
import org.example.educheck.domain.attendance.entity.AttendanceStatus;
import org.example.educheck.domain.member.entity.Member;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Slf4j
public class AbsenceAttendanceResponseDto {

    //유고결석신청번호
    private Long absenceAttendanceId;
    private Long absenceAttendanceRequesterId;
    private String absenceAttendanceRequesterName;
    private LocalDate startDate;
    private LocalDate endDate;
    private LocalDateTime createdAt;
    private AttendanceStatus category;
    private String reason;
    private LocalDateTime approveDateTime;
    private Character isApprove;
    private List<AttachmentFileResponseDto> files;

    public static AbsenceAttendanceResponseDto from(AbsenceAttendance absenceAttendance, Member student, List<AttachmentFileResponseDto> files) {
        return AbsenceAttendanceResponseDto.builder()
                .absenceAttendanceId(absenceAttendance.getId())
                .absenceAttendanceRequesterId(student.getId())
                .absenceAttendanceRequesterName(student.getName())
                .startDate(absenceAttendance.getStartTime())
                .endDate(absenceAttendance.getEndTime())
                .createdAt(absenceAttendance.getCreatedAt())
                .category(absenceAttendance.getCategory())
                .reason(absenceAttendance.getReason())
                .approveDateTime(absenceAttendance.getApproveDateTime())
                .isApprove(absenceAttendance.getIsApprove())
                .files(files)
                .build();
    }
}
