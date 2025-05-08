package com.projects.hrs.configuration;

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
    public static String EXCHANGE_NAME = "hrs-exchange";
    public static String BILL_CREATED_QUEUE = "bill.queue";
    public static String BILL_CREATED_ROUTING_KEY = "bill";

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public TopicExchange hrsExchange() {
        return new TopicExchange(EXCHANGE_NAME);
    }

    @Bean
    public Queue billCreatedQueue() {
        return new Queue(BILL_CREATED_QUEUE, true);
    }

    @Bean
    public Binding billCreatedBinding() {
        return BindingBuilder
                .bind(billCreatedQueue())
                .to(hrsExchange())
                .with(BILL_CREATED_ROUTING_KEY);
    }
}
