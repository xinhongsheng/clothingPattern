package com.xhs.clothingpatternbackend.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MJGenerateRabbitConfig {

    public static final String MJ_GENERATE_EXCHANGE = "mj.generate.exchange";
    public static final String MJ_GENERATE_QUEUE = "mj.generate.queue";
    public static final String MJ_GENERATE_ROUTING_KEY = "mj.generate";

    @Bean
    public DirectExchange mjGenerateExchange() {
        return new DirectExchange(MJ_GENERATE_EXCHANGE, true, false);
    }

    @Bean
    public Queue mjGenerateQueue() {
        return QueueBuilder.durable(MJ_GENERATE_QUEUE).build();
    }

    @Bean
    public Binding mjGenerateBinding(Queue mjGenerateQueue, DirectExchange mjGenerateExchange) {
        return BindingBuilder.bind(mjGenerateQueue)
                .to(mjGenerateExchange)
                .with(MJ_GENERATE_ROUTING_KEY);
    }
}
