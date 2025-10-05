package org.example.educheck.global.rabbitmq.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CourseNoticeRabbitMQConfig {

    @Value("${educheck.rabbitmq.exchange.notice}")
    private String exchangeName;

    @Value("${educheck.rabbitmq.queue.primary}")
    private String queueName; // 추후 다른 두개의 큐는 retryQueueName 이런식으로

    @Value("${educheck.rabbitmq.routing-key.format.receive}")
    private String bindingPattern;

    @Bean
    public TopicExchange courseNoticeExchange() {
        return new TopicExchange(exchangeName);
    }

    @Bean
    public Queue courseNoticeQueue() {
        return new Queue(queueName, true);
    }

    @Bean
    public Binding courseNoticeBinding(Queue courseNoticeQueue, TopicExchange courseNoticeExchange) {
        return BindingBuilder.bind(courseNoticeQueue)
                .to(courseNoticeExchange)
                .with(bindingPattern);
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
