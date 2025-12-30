package com.xhs.clothingpatternbackend.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ImageFusionRabbitConfig {

    public static final String IMAGE_FUSION_EXCHANGE = "image.fusion.exchange";
    public static final String IMAGE_FUSION_QUEUE = "image.fusion.queue";
    public static final String IMAGE_FUSION_ROUTING_KEY = "image.fusion";

    @Bean
    public DirectExchange imageFusionExchange() {
        return new DirectExchange(IMAGE_FUSION_EXCHANGE, true, false);
    }

    @Bean
    public Queue imageFusionQueue() {
        return QueueBuilder.durable(IMAGE_FUSION_QUEUE).build();
    }

    @Bean
    public Binding imageFusionBinding(Queue imageFusionQueue, DirectExchange imageFusionExchange) {
        return BindingBuilder.bind(imageFusionQueue)
                .to(imageFusionExchange)
                .with(IMAGE_FUSION_ROUTING_KEY);
    }
}
