package com.projects.brt.configuration;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMqConfiguration {
    @Value(value = "${rabbitmq.exchange-name}")
    public String EXCHANGE_NAME;

    @Value(value = "${rabbitmq.call-created-queue}")
    public String CALL_CREATED_QUEUE;

    @Value(value = "${rabbitmq.bill-created-queue}")
    public String BILL_CREATED_QUEUE = "bill.queue";

    @Value(value = "${rabbitmq.call-created-routing-key}")
    public String CALL_CREATED_ROUTING_KEY;

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }


    @Bean
    public Queue billQueue() {
        return new Queue(BILL_CREATED_QUEUE, true);
    }

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
}
