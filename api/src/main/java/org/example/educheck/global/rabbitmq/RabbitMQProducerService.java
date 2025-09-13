package org.example.educheck.global.rabbitmq;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class RabbitMQProducerService {

    private static final String EXCHANGE_NAME = "educheck.course.exchange";
    private static final String ROUTING_KEY_PREFIX = "course.";
    private static final String ROUTING_KEY_SUFFIX = ".notice";

    private final RabbitTemplate rabbitTemplate;

    public void sendCourseNotice(String courseName, String message) {
        String routingKey = ROUTING_KEY_PREFIX + courseName + ROUTING_KEY_SUFFIX;
        log.info("Sending course notice message to exchange : {}, routing key: {}, message: {}", EXCHANGE_NAME, routingKey, message);

        rabbitTemplate.convertAndSend(EXCHANGE_NAME, routingKey, message);
    }
}
