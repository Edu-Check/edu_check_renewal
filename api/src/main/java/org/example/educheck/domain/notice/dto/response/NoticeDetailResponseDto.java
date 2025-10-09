package org.example.educheck.domain.notice.dto.response;

import lombok.Builder;
import lombok.Getter;
import org.example.educheck.domain.member.entity.Member;
import org.example.educheck.domain.notice.entity.Notice;

import java.time.LocalDateTime;
import java.util.Optional;

@Getter
@Builder
public class NoticeDetailResponseDto {

    private final Long id;
    private final String title;
    private final String content;
    private final String authorName;
    private final LocalDateTime createdAt;

    public static NoticeDetailResponseDto from(final Notice notice) {

        String authorName = Optional.ofNullable(notice.getMember())
                .map(Member::getName)
                .orElse("관리자");

        return NoticeDetailResponseDto.builder()
                .id(notice.getId())
                .title(notice.getTitle())
                .content(notice.getContent())
                .authorName(authorName)
                .createdAt(notice.getCreatedAt())
                .build();
    }
}
