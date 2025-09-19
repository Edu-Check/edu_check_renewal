package org.example.educheck.global.rabbitmq.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class NoticeMessageDto{

    private String courseName;
    private String content;

    // 어차피 내부에서 추상화해서 제공 -> builder 사용할 이유가 없음
    public static NoticeMessageDto from(String courseName, String content){
        return new NoticeMessageDto(courseName, content);
    }

}
