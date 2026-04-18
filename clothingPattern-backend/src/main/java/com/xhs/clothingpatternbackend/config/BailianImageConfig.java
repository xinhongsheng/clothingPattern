package com.xhs.clothingpatternbackend.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@Data
@ConfigurationProperties(prefix = "dashscope.image")
public class BailianImageConfig {

    private String apiKey;

    private String baseUrl = "https://dashscope.aliyuncs.com/api/v1";

    private String model = "qwen-image-2.0-pro";

    private String size = "2048*2048";

    private Boolean watermark = false;

    private Boolean promptExtend = true;

    private String negativePrompt = "低分辨率，低画质，肢体畸形，手指畸形，画面过饱和，蜡像感，人脸无细节，过度光滑，画面具有AI感，构图混乱，文字模糊，扭曲。";
}
