package org.example.educheck.domain.notice.port.out;

import org.example.educheck.global.rabbitmq.dto.NoticeMessageDto;

public interface NoticeEventPublisherPort {

    void publish(NoticeMessageDto messageDto);
}
