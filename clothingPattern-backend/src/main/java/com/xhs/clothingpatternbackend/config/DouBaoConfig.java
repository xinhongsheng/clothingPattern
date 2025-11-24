package com.xhs.clothingpatternbackend.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * @Author: 小辛同学
 * @CreateTime: 2025-11-21
 * @Description:通义千问相关配置
 * @Version: 1.0 doubao-seedream-4-0-250828
 */
@Configuration
@Data
@ConfigurationProperties(prefix = "dou-bao.config")
public class DouBaoConfig {
    private String modelId;
    private String apiKey;
}
