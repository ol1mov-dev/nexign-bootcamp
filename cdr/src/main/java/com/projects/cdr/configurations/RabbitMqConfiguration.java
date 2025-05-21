package com.projects.cdr.configurations;


import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMqConfiguration {
//
    @Value(value = "${rabbitmq.exchange-name}")
    public String EXCHANGE_NAME;

    @Value(value = "${rabbitmq.cdr-created-queue}")
    public String CDR_CREATED_QUEUE;

    @Value(value = "${rabbitmq.cdr-created-routing-key}")
    public String CDR_CREATED_ROUTING_KEY;
//
//    @Bean
//    public Jackson2JsonMessageConverter jsonMessageConverter() {
//        return new Jackson2JsonMessageConverter();
//    }
//
//    @Bean
//    public TopicExchange cdrsExchange() {
//        return new TopicExchange(EXCHANGE_NAME);
//    }
//
//    @Bean
//    public Queue cdrCreatedQueue() {
//        return new Queue(CDR_CREATED_QUEUE, true);
//    }
//
//    @Bean
//    public Binding orderCreatedBinding() {
//        return BindingBuilder
//                .bind(cdrCreatedQueue())
//                .to(cdrsExchange())
//                .with(CDR_CREATED_ROUTING_KEY);
//    }
}
