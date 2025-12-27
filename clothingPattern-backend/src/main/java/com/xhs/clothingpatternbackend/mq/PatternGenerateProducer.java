package com.xhs.clothingpatternbackend.mq;

import com.xhs.clothingpatternbackend.config.PatternGenerateRabbitConfig;
import com.xhs.clothingpatternbackend.model.dto.pattern.PatternGenerateMessage;
import jakarta.annotation.Resource;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
public class PatternGenerateProducer {

    @Resource
    private RabbitTemplate rabbitTemplate;

    public void send(PatternGenerateMessage message) {
        rabbitTemplate.convertAndSend(
                PatternGenerateRabbitConfig.PATTERN_GENERATE_EXCHANGE,
                PatternGenerateRabbitConfig.PATTERN_GENERATE_ROUTING_KEY,
                message
        );
    }
}
