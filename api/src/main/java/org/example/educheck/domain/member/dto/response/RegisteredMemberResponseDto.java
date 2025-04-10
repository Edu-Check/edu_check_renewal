package org.example.educheck.domain.member.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.example.educheck.domain.member.entity.Member;

@Builder
@Getter
@AllArgsConstructor
public class RegisteredMemberResponseDto {
    private Long memberId;
    private String studentName;
    private String studentPhoneNumber;
    private String studentEmail;

    public static RegisteredMemberResponseDto from(Member member) {

        return RegisteredMemberResponseDto.builder()
                .memberId(member.getId())
                .studentName(member.getName())
                .studentPhoneNumber(member.getPhoneNumber())
                .studentEmail(member.getEmail())
                .build();
    }

}
