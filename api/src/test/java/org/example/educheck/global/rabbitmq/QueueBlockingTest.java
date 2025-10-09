package org.example.educheck.global.rabbitmq;

import org.example.educheck.global.fcm.service.FCMService;
import org.example.educheck.global.rabbitmq.dto.NoticeMessageDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.testcontainers.containers.RabbitMQContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.Duration;

import static org.awaitility.Awaitility.await;
import static org.mockito.Mockito.*;

@SpringBootTest
@Testcontainers
public class QueueBlockingTest {

    @Container
    static RabbitMQContainer rabbitMQContainer = new RabbitMQContainer("rabbitmq:3.9-management")
            .withAdminUser("testUser")
            .withAdminPassword("testPassword");

    @Autowired
    private AmqpAdmin amqpAdmin;

    @Value("${educheck.rabbitmq.exchange.notice}")
    private String PRIMARY_EXCHANGE;

    @Value("${educheck.rabbitmq.queue.primary}")
    private String PRIMARY_QUEUE;

    @Value("${educheck.rabbitmq.routing-key.format.send}")
    private String PRIMARY_ROUTING_KEY;

    @DynamicPropertySource
    static void configure(DynamicPropertyRegistry registry) {
        registry.add("spring.rabbitmq.host", rabbitMQContainer::getHost);
        registry.add("spring.rabbitmq.port", rabbitMQContainer::getAmqpPort);
        registry.add("spring.rabbitmq.username",() -> "testUser");
        registry.add("spring.rabbitmq.password",() -> "testPassword");
        registry.add("spring.rabbitmq.listener.simple.default-requeue-rejected", () -> "true");
    }

    @BeforeEach
    void setup() {

        TopicExchange topicExchange = new TopicExchange(PRIMARY_EXCHANGE);
        Queue queue = new Queue(PRIMARY_QUEUE);
        Binding binding = BindingBuilder.bind(queue).to(topicExchange).with(PRIMARY_ROUTING_KEY);

        amqpAdmin.declareExchange(topicExchange);
        amqpAdmin.declareQueue(queue);
        amqpAdmin.declareBinding(binding);

        amqpAdmin.purgeQueue(PRIMARY_QUEUE, true);

    }



    @Autowired
    private RabbitTemplate rabbitTemplate;

    @MockitoSpyBean
    private RabbitMQConsumerService consumer;

    @MockitoBean
    private FCMService fcmService;

    @Test
    void 큐_블로킹_상황_테스트() {
        //given
        NoticeMessageDto normalMsg1 = new NoticeMessageDto(1L, 1L, "정상 공지 1 타이틀", "정상 공지 내용");
        NoticeMessageDto poisonMsg = new NoticeMessageDto(1L, 2L, "FCM 에러 유발 공지", "FCM 에러 유발 내용");
        NoticeMessageDto normalMsg2 = new NoticeMessageDto(1L, 3L, "정상 공지 2 타이틀", "정상 공지 내용");

        doThrow(new RuntimeException("FCM 서버 에러"))
                .when(fcmService).sendNotificationToCourseStudentds(poisonMsg);

        //when
        rabbitTemplate.convertAndSend(PRIMARY_EXCHANGE, PRIMARY_ROUTING_KEY, normalMsg1);
        rabbitTemplate.convertAndSend(PRIMARY_EXCHANGE, PRIMARY_ROUTING_KEY, poisonMsg);
        rabbitTemplate.convertAndSend(PRIMARY_EXCHANGE, PRIMARY_ROUTING_KEY, normalMsg2);

        //then
        await().atMost(Duration.ofSeconds(10)).untilAsserted(() -> {
            verify(fcmService, atLeast(3)).sendNotificationToCourseStudentds(poisonMsg);
        });

        verify(fcmService, timeout(1)).sendNotificationToCourseStudentds(normalMsg1);


    }

}
