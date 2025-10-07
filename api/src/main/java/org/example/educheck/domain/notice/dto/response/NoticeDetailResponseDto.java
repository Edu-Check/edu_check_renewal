package org.example.educheck.domain.notice.dto.response;

import lombok.Builder;
import lombok.Getter;
import org.example.educheck.domain.notice.entity.Notice;

import java.time.LocalDateTime;

@Getter
@Builder
public class NoticeDetailResponseDto {

    private final Long id;
    private final String title;
    private final String content;
    private final String authorName;
    private final LocalDateTime createdAt;

    public static NoticeDetailResponseDto from(final Notice notice) {
        return NoticeDetailResponseDto.builder()
                .id(notice.getId())
                .title(notice.getTitle())
                .content(notice.getContent())
                .authorName(notice.getMember().getName())
                .createdAt(notice.getCreatedAt())
                .build();
    }
}
