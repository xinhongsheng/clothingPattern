package com.xhs.clothingpatternbackend.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * @Author: 小辛同学
 * @CreateTime: 2025-12-02
 * @Description:
 * @Version: 1.0
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "dashscope.wan")
public class WanConfig {
    // API密钥
    private String apiKey;
    // 北京地域API地址
    private String beijingSubmitUrl; // https://dashscope.aliyuncs.com/api/v1/services/aigc/image2image/image-synthesis
    private String beijingQueryUrl;  // https://dashscope.aliyuncs.com/api/v1/tasks/{taskId}
    // 新加坡地域API地址
    private String singaporeSubmitUrl; // https://dashscope-intl.aliyuncs.com/api/v1/services/aigc/image2image/image-synthesis
    private String singaporeQueryUrl;  // https://dashscope-intl.aliyuncs.com/api/v1/tasks/{taskId}
    // 默认地域（可配置为BEIJING/SINGAPORE）
    private String defaultRegion;
}
