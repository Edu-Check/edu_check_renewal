package org.example.educheck.global.rabbitmq.dto;

import lombok.*;
import org.example.educheck.domain.notice.entity.Notice;

@Getter
@NoArgsConstructor
@EqualsAndHashCode
public class NoticeMessageDto{ // MQ서버와 웹서버간 전달용 메세지

    private static final String SOURCE_TABLE_NOTICE = "notice";


    private Long courseId;
    private Long noticeId;
    private String title;
    private String content;
    private String sourceTable = SOURCE_TABLE_NOTICE;

    public NoticeMessageDto(Long courseId, Long noticeId, String title, String content) {
        this.courseId = courseId;
        this.noticeId = noticeId;
        this.title = title;
        this.content = content;
        this.sourceTable = SOURCE_TABLE_NOTICE;
    }

    public static NoticeMessageDto from(Long courseId, Notice notice){
        return new NoticeMessageDto(courseId, notice.getId(), notice.getTitle(), notice.getContent());
    }


}
