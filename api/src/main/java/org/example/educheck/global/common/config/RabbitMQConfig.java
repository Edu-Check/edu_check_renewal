package org.example.educheck.global.common.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    private static final String EXCHANGE_NAME = "educheck.course.exchange";
    private static final String QUEUE_NAME = "educheck.course.notice.queue";
    private static final String ROUTING_KEY = "course.#";

    @Bean
    public TopicExchange courseExchange() {
        return new TopicExchange(EXCHANGE_NAME);
    }

    @Bean
    public Queue courseNoticeQueue() {
        return new Queue(QUEUE_NAME, true);
    }

    @Bean
    public Binding courseBinding(Queue courseNoticeQueue, TopicExchange courseExchange) {
        return BindingBuilder.bind(courseNoticeQueue)
                .to(courseExchange)
                .with(ROUTING_KEY);
    }

}
