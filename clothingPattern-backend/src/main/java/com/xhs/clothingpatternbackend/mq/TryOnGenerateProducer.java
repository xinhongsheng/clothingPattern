package com.xhs.clothingpatternbackend.mq;

import com.xhs.clothingpatternbackend.config.TryOnRabbitConfig;
import com.xhs.clothingpatternbackend.model.dto.tryon.TryOnGenerateMessage;
import jakarta.annotation.Resource;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
public class TryOnGenerateProducer {

    @Resource
    private RabbitTemplate rabbitTemplate;

    public void send(TryOnGenerateMessage message) {
        rabbitTemplate.convertAndSend(
                TryOnRabbitConfig.TRY_ON_EXCHANGE,
                TryOnRabbitConfig.TRY_ON_ROUTING_KEY,
                message
        );
    }
}
