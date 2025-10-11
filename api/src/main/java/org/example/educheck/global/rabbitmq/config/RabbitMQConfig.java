package org.example.educheck.global.rabbitmq.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    @Value("${educheck.rabbitmq.exchange.notice}")
    private String primaryExchange;

    @Value("${educheck.rabbitmq.queue.primary}")
    private String primaryQueue;

    @Value("${educheck.rabbitmq.routing-key.format.receive}")
    private String primaryRoutingKeyReceive;

    @Value("${educheck.rabbitmq.exchange.dlx}")
    private String dlxExchange;

    @Value("${educheck.rabbitmq.queue.dlq}")
    private String dlqQueue;

    @Value("${educheck.rabbitmq.routing-key.format.dlq}")
    private String dlqRoutingKey;



    @Bean
    public TopicExchange courseNoticeExchange() {
        return new TopicExchange(primaryExchange);
    }

    @Bean
    public Queue courseNoticeQueue() {
        return QueueBuilder.durable(primaryQueue)
                .withArgument("x-dead-letter-exchange", dlxExchange)
                .withArgument("x-dead-letter-routing-key", dlqRoutingKey)
                .build();
    }

    @Bean
    public Binding courseNoticeBinding(Queue courseNoticeQueue, TopicExchange courseNoticeExchange) {
        return BindingBuilder.bind(courseNoticeQueue)
                .to(courseNoticeExchange)
                .with(primaryRoutingKeyReceive);
    }

    @Bean
    public DirectExchange dlxExchange() {
        return new DirectExchange(dlxExchange);
    }

    @Bean
    public Queue dlqQueue() {
        return new Queue(dlqQueue, true);
    }

    @Bean
    public Binding dlqBinding(Queue dlqQueue, DirectExchange dlxExchange) {
        return BindingBuilder.bind(dlqQueue)
                .to(dlxExchange)
                .with(dlqRoutingKey);
    }

    @Bean
    public MessageConverter messageConverter() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        return new Jackson2JsonMessageConverter(objectMapper);
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory, MessageConverter messageConverter) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(messageConverter);
        return rabbitTemplate;
    }
}
