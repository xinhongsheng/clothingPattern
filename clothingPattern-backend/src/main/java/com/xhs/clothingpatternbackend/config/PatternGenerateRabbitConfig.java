package com.xhs.clothingpatternbackend.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;

@Configuration
@EnableRabbit
public class PatternGenerateRabbitConfig {

    public static final String PATTERN_GENERATE_EXCHANGE = "pattern.generate.exchange";
    public static final String PATTERN_GENERATE_QUEUE = "pattern.generate.queue";
    public static final String PATTERN_GENERATE_ROUTING_KEY = "pattern.generate";

    @Bean
    public DirectExchange patternGenerateExchange() {
        return new DirectExchange(PATTERN_GENERATE_EXCHANGE, true, false);
    }

    @Bean
    public Queue patternGenerateQueue() {
        return QueueBuilder.durable(PATTERN_GENERATE_QUEUE).build();
    }

    @Bean
    public Binding patternGenerateBinding(Queue patternGenerateQueue, DirectExchange patternGenerateExchange) {
        return BindingBuilder.bind(patternGenerateQueue)
                .to(patternGenerateExchange)
                .with(PATTERN_GENERATE_ROUTING_KEY);
    }

    @Bean
    public MessageConverter jacksonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
