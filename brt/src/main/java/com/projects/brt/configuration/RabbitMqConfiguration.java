package com.projects.brt.configuration;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMqConfiguration {
    public static final String EXCHANGE_NAME = "brt_exchange";
    public static final String CALL_CREATED_QUEUE = "call.queue";
    public static final String CALL_CREATED_ROUTING_KEY = "call";

    @Bean
    public TopicExchange brtExchange() {
        return new TopicExchange(EXCHANGE_NAME);
    }

    @Bean
    public Queue callCreatedQueue() {
        return new Queue(CALL_CREATED_QUEUE, true);
    }

    @Bean
    public Binding orderCreatedBinding() {
        return BindingBuilder
                .bind(callCreatedQueue())
                .to(brtExchange())
                .with(CALL_CREATED_ROUTING_KEY);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
