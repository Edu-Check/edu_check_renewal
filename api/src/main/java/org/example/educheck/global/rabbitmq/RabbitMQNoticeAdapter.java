package org.example.educheck.global.rabbitmq;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.educheck.domain.notice.port.out.NoticeEventPublisherPort;
import org.example.educheck.global.rabbitmq.dto.NoticeMessageDto;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
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

    @Retryable(
            value = {AmqpException.class},
            maxAttempts = 2,
            backoff = @Backoff(delay = 1000, multiplier = 2)
    )
    @Override
    public void publish(NoticeMessageDto messageDto) {
        String routingKey = getRoutingKey(messageDto.getCourseId());
            rabbitTemplate.convertAndSend(exchangeName, routingKey, messageDto);
    }

    @Recover
    public void recover(AmqpException e, NoticeMessageDto messageDto) {
        log.error("재시도 후 최종 실패 - courseId: {}", messageDto.getCourseId(), e);

        //fail 테이블에 저장

        //TODO: 슬랙알림 연동
    }

    private String getRoutingKey(Long courseId) {
        return String.format(routingKeyFormat, courseId);
    }

}
