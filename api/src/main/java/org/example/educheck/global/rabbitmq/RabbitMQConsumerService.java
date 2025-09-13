package org.example.educheck.global.rabbitmq;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.educheck.global.rabbitmq.dto.NoticeMessageDto;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class RabbitMQConsumerService {

    private final SimpMessagingTemplate messagingTemplate;
    private static final String TOPIC_PREFIX = "/topic/course/";

    @RabbitListener(queues = "educheck.course.notice.queue")
    public void receiveCourseNotice(NoticeMessageDto messageDto){
        log.info("messageDto : {}", messageDto);

        String destination = TOPIC_PREFIX + messageDto.getCourseName();

        messagingTemplate.convertAndSend(destination, messageDto.getContent());
        log.info("Message sent to WebSocket destination: {}, message: {}", destination, messageDto.getContent());

    }

}
