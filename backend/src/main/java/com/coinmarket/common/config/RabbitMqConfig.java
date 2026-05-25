package com.coinmarket.common.config;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMqConfig {

    public static final String EXCHANGE_ORDER = "order.exchange";
    public static final String QUEUE_ORDER_PAYMENT = "order.payment.queue";
    public static final String QUEUE_ORDER_NOTIFICATION = "order.notification.queue";
    public static final String ROUTING_KEY_PAYMENT = "order.payment";

    @Bean
    public TopicExchange orderExchange() {
        return new TopicExchange(EXCHANGE_ORDER);
    }

    @Bean
    public Queue orderPaymentQueue() {
        return QueueBuilder.durable(QUEUE_ORDER_PAYMENT).build();
    }

    @Bean
    public Queue orderNotificationQueue() {
        return QueueBuilder.durable(QUEUE_ORDER_NOTIFICATION).build();
    }

    @Bean
    public Binding paymentBinding() {
        return BindingBuilder.bind(orderPaymentQueue())
                .to(orderExchange())
                .with(ROUTING_KEY_PAYMENT);
    }
}
