package org.example.educheck.domain.member.student.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;


@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
public class StudentInfoResponseDto {

    private Long memberId;
    private Long studentId;
    private String studentName;
    private String studentEmail;
    private String studentPhoneNumber;

}
