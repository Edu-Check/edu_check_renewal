package org.example.educheck.domain.member.staff.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
public class UpdateStudentRegistrationStatusRequestDto {

    private LocalDate dropDate;
    private LocalDate completionDate;
}
