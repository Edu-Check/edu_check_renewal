package org.example.educheck.domain.notice.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class NoticeMessageResponseDto {

    private Long courseId;
    private Long noticeId;
    private String title;
    private String content;
}
