package org.example.educheck.global.common.config;

import com.fasterxml.jackson.databind.ObjectMapper;
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
public class RabbitMQConfig {

    @Value("${rabbitmq.exchange.name}")
    private String exchangeName;

    @Value("${rabbitmq.queue.name}")
    private String queueName;

    @Value("${rabbitmq.binding-key}")
    private String bindingKey; // 메시지를 어떤 큐로 보낼지 결정하는 규칙

    @Bean
    public TopicExchange courseExchange() {
        return new TopicExchange(exchangeName);
    }

    @Bean
    public Queue courseNoticeQueue() {
        return new Queue(queueName, true);
    }

    @Bean
    public Binding courseBinding(Queue courseNoticeQueue, TopicExchange courseExchange) {
        return BindingBuilder.bind(courseNoticeQueue)
                .to(courseExchange)
                .with(bindingKey);
    }

    @Bean
    public MessageConverter Jackson2JsonMessageConverter() {
        return new Jackson2JsonMessageConverter(new ObjectMapper());
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory, MessageConverter messageConverter) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(messageConverter);
        return rabbitTemplate;
    }

}
