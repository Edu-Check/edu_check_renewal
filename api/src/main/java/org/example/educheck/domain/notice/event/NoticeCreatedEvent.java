package org.example.educheck.domain.notice.event;

import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.example.educheck.global.rabbitmq.dto.NoticeMessageDto;

@Getter
@RequiredArgsConstructor
public class NoticeCreatedEvent {

    private final NoticeMessageDto noticeMessageDto;

    public static NoticeCreatedEvent create(NoticeMessageDto noticeMessageDto) {
        return new NoticeCreatedEvent(noticeMessageDto);
    }
}
