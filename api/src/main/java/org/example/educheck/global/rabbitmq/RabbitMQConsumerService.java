package org.example.educheck.global.rabbitmq;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.educheck.global.fcm.service.FCMService;
import org.example.educheck.global.rabbitmq.dto.NoticeMessageDto;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class RabbitMQConsumerService {

    private final FCMService fcmService;

    @RabbitListener(queues = "${educheck.rabbitmq.queue.primary}")
    public void receiveCourseNotice(NoticeMessageDto messageDto){

        log.info("Primary Queue로부터 받은 messageDto : {}", messageDto.toString());

        try {
            fcmService.sendNotificationToCourseStudentds(messageDto);
            log.info("FCM로 메시지 전송 성공 courseId : {}", messageDto.getCourseId());
        } catch (Exception e) {
            log.error("FCM로 메시지 전송 실패 courseId: {}, Error:{} ", messageDto.getCourseId(), e.getMessage());
//            throw e; // 예외를 다시 던져서 Spring AMQP가 재시도 및 DLQ 처리를 하도록 함
            throw new RuntimeException("메시지 처리 실패", e);
        }
    }

    @RabbitListener(queues = "${educheck.rabbitmq.queue.dlq}")
    public void receiveDeadLetter(Message failedMessage) {
        log.info("failedMessage : {}", failedMessage.toString());

        String originalQueue = "N/A";
        String reason = "N/A";

        List<Map<String, Object>> deathHeader = failedMessage.getMessageProperties().getHeader("x-dead");
        if (deathHeader != null && !deathHeader.isEmpty()) {
            Map<String, Object> firstDeath = deathHeader.get(0);
            originalQueue = (String) firstDeath.get("queue");
            reason = (String) firstDeath.get("reason");
        }

        log.error("=================== Dead Letter ===================");
        log.error("Original Queue: {}", originalQueue);
        log.error("Reason: {}", reason);
        log.error("Failed Message Body: {}", new String(failedMessage.getBody()));
        log.error("=====================================================");

        //TODO: DLQ 처리 로직 (관리자아게 알림, DB 기록 등)
    }

}
