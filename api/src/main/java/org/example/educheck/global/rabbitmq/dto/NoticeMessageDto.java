package org.example.educheck.global.rabbitmq.dto;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
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
