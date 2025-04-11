package org.example.educheck.domain.attendance.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.educheck.domain.attendance.entity.AttendanceStatus;

import java.time.LocalDate;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AttendanceUpdateRequestDto {

    private AttendanceStatus status;
    private LocalDate date;
}
