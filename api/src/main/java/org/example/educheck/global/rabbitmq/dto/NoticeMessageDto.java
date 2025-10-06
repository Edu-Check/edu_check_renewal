package org.example.educheck.global.rabbitmq.dto;

import lombok.*;

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
    public static NoticeMessageDto from(Long courseId, Long noticeId, String title, String content){
        return new NoticeMessageDto(courseId, noticeId, title, content);
    }

}
