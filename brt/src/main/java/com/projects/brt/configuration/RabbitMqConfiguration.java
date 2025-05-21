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
    @Value(value = "${rabbitmq.brt-exchange-name}")
    public String BRT_EXCHANGE_NAME;

    @Value(value = "${rabbitmq.call-created-queue}")
    public String CALL_CREATED_QUEUE;

    @Value(value = "${rabbitmq.bill-created-queue}")
    public String BILL_CREATED_QUEUE;

    @Value(value = "${rabbitmq.call-created-routing-key}")
    public String CALL_CREATED_ROUTING_KEY;

    @Value(value = "${rabbitmq.cdr-exchange-name}")
    public String CDR_EXCHANGE_NAME;

    @Value(value = "${rabbitmq.cdr-created-queue}")
    public String CDR_CREATED_QUEUE;

    @Value(value = "${rabbitmq.cdr-created-routing-key}")
    public String CDR_CREATED_ROUTING_KEY;

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
        return new TopicExchange(BRT_EXCHANGE_NAME);
    }

    @Bean
    public Queue callCreatedQueue() {
        return new Queue(CALL_CREATED_QUEUE, true);
    }

    @Bean
    public TopicExchange cdrsExchange() {
        return new TopicExchange(CDR_EXCHANGE_NAME);
    }

    @Bean
    public Queue cdrCreatedQueue() {
        return new Queue(CDR_CREATED_QUEUE, true);
    }

    @Bean
    public Binding orderCreatedBindingBrt() {
        return BindingBuilder
                .bind(callCreatedQueue())
                .to(brtExchange())
                .with(CALL_CREATED_ROUTING_KEY);
    }


    @Bean
    public Binding orderCreatedBindingCdr() {
        return BindingBuilder
                .bind(cdrCreatedQueue())
                .to(cdrsExchange())
                .with(CDR_CREATED_ROUTING_KEY);
    }
}
