package org.example.educheck.global.rabbitmq;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.educheck.domain.notice.port.out.NoticeEventPublisherPort;
import org.example.educheck.global.rabbitmq.dto.NoticeMessageDto;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class RabbitMQNoticeAdapter implements NoticeEventPublisherPort {

    @Value("${educheck.rabbitmq.exchange.notice}")
    private String exchangeName;

    @Value("${educheck.rabbitmq.routing-key.format.send}")
    private String routingKeyFormat;

    private final RabbitTemplate rabbitTemplate;


    @Override
    public void publish(NoticeMessageDto messageDto) {
        String routingKey = getRoutingKey(messageDto.getCourseId());
        log.info("rabbitmq event publish");
        rabbitTemplate.convertAndSend(exchangeName, routingKey, messageDto);
    }

    private String getRoutingKey(Long courseId) {
        return String.format(routingKeyFormat, courseId);
    }

}
