package com.projects.cdr.configuration;

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
    public static final String EXCHANGE_NAME = "cdr_exchange";
    public static final String CDR_CREATED_QUEUE = "cdr.queue";
    public static final String CDR_CREATED_ROUTING_KEY = "cdr";

    @Bean
    public TopicExchange cdrsExchange() {
        return new TopicExchange(EXCHANGE_NAME);
    }

    @Bean
    public Queue cdrCreatedQueue() {
        return new Queue(CDR_CREATED_QUEUE, true);
    }

    @Bean
    public Binding orderCreatedBinding() {
        return BindingBuilder
                .bind(cdrCreatedQueue())
                .to(cdrsExchange())
                .with(CDR_CREATED_ROUTING_KEY);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
