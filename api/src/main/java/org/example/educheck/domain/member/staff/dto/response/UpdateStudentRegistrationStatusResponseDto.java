package org.example.educheck.domain.member.staff.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
public class UpdateStudentRegistrationStatusResponseDto {

    private Long studentId;
    private Long courseId;
    private String studentName;
    private String courseName;
    private LocalDate dropDate;
    private LocalDate completionDate;

    public static UpdateStudentRegistrationStatusResponseDto from(
            Long studentId,
            Long courseId,
            String studentName,
            String courseName,
            LocalDate dropDate,
            LocalDate completionDate) {

        return UpdateStudentRegistrationStatusResponseDto.builder()
                .studentId(studentId)
                .courseId(courseId)
                .studentName(studentName)
                .courseName(courseName)
                .dropDate(dropDate)
                .completionDate(completionDate)
                .build();
    }

}
