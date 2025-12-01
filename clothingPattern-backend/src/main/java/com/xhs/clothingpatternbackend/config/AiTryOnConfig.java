package com.xhs.clothingpatternbackend.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * @Author: 小辛同学
 * @CreateTime: 2025-12-01
 * @Description:
 * @Version: 1.0
 */
@Configuration
@Data
@ConfigurationProperties(prefix = "dashscope.aitryon")
public class AiTryOnConfig {
    private String apiKey;
    private String submitUrl;
    private String taskQueryUrl;
}
