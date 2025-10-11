package org.example.educheck.global.rabbitmq.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.educheck.global.common.exception.custom.TransientMessageException;
import org.example.educheck.global.common.exception.custom.common.GlobalException;
import org.example.educheck.global.fcm.service.FCMService;
import org.example.educheck.global.rabbitmq.dto.NoticeMessageDto;
import org.example.educheck.global.rabbitmq.entity.DeadLetterMessage;
import org.example.educheck.global.rabbitmq.repository.DeadLetterMessageRepository;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class RabbitMQConsumerService {

    private final FCMService fcmService;
    private final DeadLetterMessageRepository deadLetterMessageRepository;
    private final ObjectMapper objectMapper;

    @RabbitListener(queues = "${educheck.rabbitmq.queue.primary}")
    public void receiveCourseNotice(NoticeMessageDto messageDto){

        log.info("Primary Queue로부터 받은 messageDto : {}", messageDto.toString());

        try {
            fcmService.sendNotificationToCourseStudentds(messageDto);
            log.info("FCM로 메시지 전송 성공 courseId : {}", messageDto.getCourseId());
        } catch (GlobalException e) {
            if(e.isFatalError()) {
                log.error("[Fatal Error] 메시지 처리 실패. 재시도 x DLQ 전송. courseId: {}, Error: {}", messageDto.getCourseId(), e.getMessage(), e);
                throw new AmqpRejectAndDontRequeueException(e.getMessage(), e);
            } else {
                log.error("[Transient Error] 메시지 처리 실패. Spring AMQP 재시도 정책 시작. courseId: {}, Error: {}",  messageDto.getCourseId(), e.getMessage(), e);
                throw new TransientMessageException(e.getMessage(), e);
            }
        } catch (Exception e) {
            log.error("[Unknown Error] 예상치 못한 오류 발생. courseId: {}, Error:{} ", messageDto.getCourseId(), e.getMessage());
//            throw e; // 예외를 다시 던져서 Spring AMQP가 재시도 및 DLQ 처리를 하도록 함
            throw new RuntimeException("메시지 처리 실패", e);
        }
    }

    @RabbitListener(queues = "${educheck.rabbitmq.queue.dlq}")
    public void receiveDeadLetter(Message failedMessage) {
        log.info("failedMessage : {}", failedMessage.toString());

        String originalQueue = "N/A";
        String reason = "N/A";

        List<Map<String, Object>> deathHeader = failedMessage.getMessageProperties().getHeader("x-death");
        if (deathHeader != null && !deathHeader.isEmpty()) {
            Map<String, Object> firstDeath = deathHeader.get(0);
            originalQueue = (String) firstDeath.get("queue");
            reason = (String) firstDeath.get("reason");
        }

        String failedMessageBody =  new String(failedMessage.getBody(), StandardCharsets.UTF_8);


        log.error("=================== Dead Letter ===================");
        log.error("Original Queue: {}", originalQueue);
        log.error("Reason: {}", reason);
        log.error("Failed Message Body: {}", failedMessageBody);
        log.error("=====================================================");

        //TODO: DLQ 처리 로직 (관리자아게 알림, DB 기록 등)
        try {
            NoticeMessageDto messageDto = objectMapper.readValue(failedMessageBody, NoticeMessageDto.class);

            DeadLetterMessage deadLetter = DeadLetterMessage.create(originalQueue, failedMessageBody, reason, messageDto.getSourceTable(), messageDto.getNoticeId());
            deadLetterMessageRepository.save(deadLetter);
            log.info("Dead Letter Message DB에 저장. ID : {}", deadLetter.getId());
        } catch (Exception e) {
            log.error("Dead Letter Message 저장/직렬화 실패. Body: {}", failedMessageBody, e);

            DeadLetterMessage deadLetter = DeadLetterMessage.builder()
                    .originalQueue(originalQueue)
                    .reason("PARSING_FAILED: " + reason)
                    .messageBody(failedMessageBody)
                    .build();
            deadLetterMessageRepository.save(deadLetter);

        }
    }

}
