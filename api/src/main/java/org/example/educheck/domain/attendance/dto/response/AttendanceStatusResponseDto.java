package org.example.educheck.domain.attendance.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.educheck.domain.attendance.entity.AttendanceStatus;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class AttendanceStatusResponseDto {
    private AttendanceStatus attendanceStatus;
}
