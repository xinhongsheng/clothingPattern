package com.xhs.clothingpatternbackend.mq;

import com.xhs.clothingpatternbackend.config.ImageFusionRabbitConfig;
import com.xhs.clothingpatternbackend.model.dto.imagefusion.ImageFusionGenerateMessage;
import jakarta.annotation.Resource;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
public class ImageFusionGenerateProducer {

    @Resource
    private RabbitTemplate rabbitTemplate;

    public void send(ImageFusionGenerateMessage message) {
        rabbitTemplate.convertAndSend(
                ImageFusionRabbitConfig.IMAGE_FUSION_EXCHANGE,
                ImageFusionRabbitConfig.IMAGE_FUSION_ROUTING_KEY,
                message
        );
    }
}
