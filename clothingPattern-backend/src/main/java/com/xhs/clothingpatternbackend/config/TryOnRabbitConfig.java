package com.xhs.clothingpatternbackend.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TryOnRabbitConfig {

    public static final String TRY_ON_EXCHANGE = "try.on.exchange";
    public static final String TRY_ON_QUEUE = "try.on.queue";
    public static final String TRY_ON_ROUTING_KEY = "try.on";

    @Bean
    public DirectExchange tryOnExchange() {
        return new DirectExchange(TRY_ON_EXCHANGE, true, false);
    }

    @Bean
    public Queue tryOnQueue() {
        return QueueBuilder.durable(TRY_ON_QUEUE).build();
    }

    @Bean
    public Binding tryOnBinding(Queue tryOnQueue, DirectExchange tryOnExchange) {
        return BindingBuilder.bind(tryOnQueue)
                .to(tryOnExchange)
                .with(TRY_ON_ROUTING_KEY);
    }
}
