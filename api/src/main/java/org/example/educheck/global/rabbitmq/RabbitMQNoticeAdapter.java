package org.example.educheck.global.rabbitmq;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.educheck.domain.notice.port.out.NoticeEventPublisherPort;
import org.example.educheck.global.common.entity.FailStatus;
import org.example.educheck.global.common.entity.ProducerFailedEvent;
import org.example.educheck.global.common.repotiroy.ProducerFailedEventRepository;
import org.example.educheck.global.rabbitmq.dto.NoticeMessageDto;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class RabbitMQNoticeAdapter implements NoticeEventPublisherPort {

    @Value("${educheck.rabbitmq.exchange.notice}")
    private String exchangeName;

    @Value("${educheck.rabbitmq.routing-key.format.send}")
    private String routingKeyFormat;

    private final RabbitTemplate rabbitTemplate;

    private final ObjectMapper objectMapper;

    private final ProducerFailedEventRepository producerFailedEventRepository;

    @Retryable(
            value = {AmqpException.class},
            maxAttempts = 3,
            backoff = @Backoff(delay = 1000, multiplier = 2)
    )
    @Override
    public void publish(NoticeMessageDto messageDto) {
        String routingKey = getRoutingKey(messageDto.getCourseId());
        try {
            rabbitTemplate.convertAndSend(exchangeName, routingKey, messageDto);
            log.debug("메시지 전송 성공 - courseId: {}, routingKey: {}", messageDto.getCourseId(), routingKey);
        } catch (AmqpException e) {
            log.warn("메시지 전송 실패 - courseId: {}, 재시도 예정", messageDto.getCourseId());
            throw e;
        }
    }

    @Recover
    public void recover(AmqpException e, NoticeMessageDto messageDto) {
        log.error("재시도 후 최종 실패 - courseId: {}", messageDto.getCourseId(), e);

        ProducerFailedEvent failedNotice = ProducerFailedEvent.builder()
                .entityType("NOTICE")
                .entityId(String.valueOf(messageDto.getNoticeId()))
                .targetExchange(exchangeName)
                .targetRoutingKey(getRoutingKey(messageDto.getCourseId()))
                .payload(toJson(messageDto))
                .errorMessage(e.getMessage())
                .status(FailStatus.PENDING)
                .retryCount(0)
                .createdAt(LocalDateTime.now())
                .build();

        producerFailedEventRepository.save(failedNotice);
        //TODO: 슬랙알림 연동
    }

    private String getRoutingKey(Long courseId) {
        return String.format(routingKeyFormat, courseId);
    }

    private String toJson(NoticeMessageDto messageDto) {
        try {
            return objectMapper.writeValueAsString(messageDto);
        } catch (JsonProcessingException e) {
            log.error("JSON 변환 실패", e);
            return messageDto.toString();
        }
    }

}
