package org.example.educheck.domain.member.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;
import org.springframework.web.bind.annotation.RequestPart;

import java.time.LocalDate;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class UpdateMyProfileRequestDto {
    @Pattern(regexp = "^\\d{3}-\\d{3,4}-\\d{4}$", message = "전화번호 형식이 올바르지 않습니다.")
    public String phoneNumber;
    @PastOrPresent(message = "생년월일은 오늘 날짜를 초과할 수 없습니다.")
    public LocalDate birthDate;
    @NotBlank
    public String currentPassword;
    @Length(min = 8, message = "비밀번호은 8개 자리 이상으로 입력할 수 있습니다.")
    public String newPassword;
}
