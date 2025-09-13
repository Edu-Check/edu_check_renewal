package org.example.educheck.global.rabbitmq.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class NoticeMessageDto{

    String courseName;
    String content;

    public static NoticeMessageDto from(String courseName, String content){
        return NoticeMessageDto.builder()
                .courseName(courseName)
                .content(content)
                .build();
    }

}
