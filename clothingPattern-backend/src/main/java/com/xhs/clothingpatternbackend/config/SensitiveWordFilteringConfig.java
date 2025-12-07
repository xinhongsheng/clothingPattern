package com.xhs.clothingpatternbackend.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * @Author: 小辛同学
 * @CreateTime: 2025-12-07
 * @Description: al
 * @Version: 1.0
 */
@Configuration
@Data
@ConfigurationProperties(prefix = "sensitive.word.filtering")
public class SensitiveWordFilteringConfig {
    private String AppKey;
    private String AppSecret;
    private String AppCode;
    private String host;
    private String path;
}
