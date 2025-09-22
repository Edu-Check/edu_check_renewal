package org.example.educheck.domain.notice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.educheck.domain.notice.dto.SendNoticeRequestDto;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class NoticeService {

    @Value("${educheck.rabbitmq.exchange.notice}")
    private String exchangeName;

    @Value("${educheck.rabbitmq.routing-key.format.send}")
    private String routingKeyFormat;

    private final RabbitTemplate rabbitTemplate;

    // message를 받아서 RabbitMQ에 발행
    public void sendCourseNotice(Long courseId, SendNoticeRequestDto noticeRequestDto) {
        //TODO: courseId에 권한이 있는 관리자인지 확인
        String routingKey = getRoutingKey(courseId);

        rabbitTemplate.convertAndSend(exchangeName, routingKey, noticeRequestDto);
    }

    private String getRoutingKey(Long courseId) {
        return String.format(routingKeyFormat, courseId);
    }


}
