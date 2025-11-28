package com.xhs.clothingpatternbackend.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * @Author: 小辛同学
 * @CreateTime: 2025-11-28
 * @Description: Midjourney API配置
 * @Version: 1.0
 */
@Configuration
@Data
@ConfigurationProperties(prefix = "mj.api")
public class MJConfig {
    
    /**
     * API Token
     */
    private String token;
    
    /**
     * API基础URL
     */
    private String url;
    
    /**
     * 请求超时时间（秒）
     */
    private Integer timeout = 300;
}

