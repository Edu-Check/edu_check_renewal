package org.example.educheck.domain.notice.dto.response;

import lombok.Builder;
import lombok.Getter;
import org.example.educheck.domain.notice.entity.Notice;

import java.time.LocalDateTime;

@Getter
@Builder
public class NoticeListResponseDto {

    private final Long id;
    private final String title;
    private final String authorName;
    private final LocalDateTime createdAt;

    public static NoticeListResponseDto from(final Notice notice) {
        return NoticeListResponseDto.builder()
                .id(notice.getId())
                .title(notice.getTitle())
                .authorName(notice.getMember().getName())
                .createdAt(notice.getCreatedAt())
                .build();
    }
}
