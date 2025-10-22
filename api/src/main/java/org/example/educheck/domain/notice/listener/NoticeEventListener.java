package org.example.educheck.domain.notice.listener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.educheck.domain.notice.event.NoticeCreatedEvent;
import org.example.educheck.domain.notice.port.out.NoticeEventPublisherPort;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class NoticeEventListener {

    private final NoticeEventPublisherPort noticeEventPublisherPort;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleNoticeCreatedEvent(NoticeCreatedEvent event) {
        log.info("noticeEventListener 수신 : {}", event);
        noticeEventPublisherPort.publish(event.getNoticeMessageDto());
    }
}
