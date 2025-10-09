package org.example.educheck.global.rabbitmq.dto;

import lombok.*;
import org.example.educheck.domain.notice.entity.Notice;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class NoticeMessageDto{ // MQ서버와 웹서버간 전달용 메세지

    private Long courseId;
    private Long noticeId;
    private String title;
    private String content;

    // 어차피 내부에서 추상화해서 제공 -> builder 사용할 이유가 없음
    public static NoticeMessageDto from(Long courseId, Notice notice){
        return new NoticeMessageDto(courseId, notice.getId(), notice.getTitle(), notice.getContent());
    }

}
