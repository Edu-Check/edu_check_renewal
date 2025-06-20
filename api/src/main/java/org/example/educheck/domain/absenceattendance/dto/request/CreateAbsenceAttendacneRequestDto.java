package org.example.educheck.domain.absenceattendance.dto.request;

import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.example.educheck.domain.attendance.entity.AttendanceStatus;

import java.time.LocalDate;
import java.util.List;

@NoArgsConstructor
@Setter
@Getter
public class CreateAbsenceAttendacneRequestDto {

    @NotEmpty
    private String reason;
    @NotEmpty
    private LocalDate startDate;
    @NotEmpty
    private LocalDate endDate;
    @NotEmpty
    private AttendanceStatus category;
    private List<FileUploadInfo> files;

    @Getter
    @Setter
    public static class FileUploadInfo {
        private String originalName;
        private String url;
        private String s3Key;
        private String mime;
    }
}
