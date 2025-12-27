package com.xhs.clothingpatternbackend.mq;

import com.xhs.clothingpatternbackend.config.MJGenerateRabbitConfig;
import com.xhs.clothingpatternbackend.model.dto.mj.MJGenerateMessage;
import jakarta.annotation.Resource;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
public class MJGenerateProducer {

    @Resource
    private RabbitTemplate rabbitTemplate;

    public void send(MJGenerateMessage message) {
        rabbitTemplate.convertAndSend(
                MJGenerateRabbitConfig.MJ_GENERATE_EXCHANGE,
                MJGenerateRabbitConfig.MJ_GENERATE_ROUTING_KEY,
                message
        );
    }
}
