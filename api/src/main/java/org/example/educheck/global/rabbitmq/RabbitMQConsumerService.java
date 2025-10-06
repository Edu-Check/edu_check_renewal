package org.example.educheck.global.rabbitmq;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.educheck.global.fcm.service.FCMService;
import org.example.educheck.global.rabbitmq.dto.NoticeMessageDto;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class RabbitMQConsumerService {

    private final FCMService fcmService;

    @RabbitListener(queues = "${educheck.rabbitmq.queue.primary}")
    public void receiveCourseNotice(NoticeMessageDto messageDto){
        log.info("messageDto : {}", messageDto);

        fcmService.sendNotificationToCourseStudentds(messageDto);

    }

}
