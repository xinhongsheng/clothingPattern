package com.xhs.clothingpatternbackend.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * @Author: 小辛同学
 * @CreateTime: 2025-08-29
 * @Description:跨域配置
 * @Version: 1.0
 */
@Configuration
public class CorsConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        // 1. 配置需要跨域的接口路径（/** 表示所有接口，可按需缩小范围如 /api/**）
        registry.addMapping("/**")
                // 2. 允许的前端域名（精准匹配，支持多个域名用逗号分隔）
//                .allowedOriginPatterns("http://bs.xinxiangyang.work")
                .allowedOriginPatterns("*")
                // 3. 允许发送Cookie（前后端需一致开启，前端axios需设withCredentials: true）
                .allowCredentials(true)
                // 4. 允许的HTTP方法（覆盖常用方法，OPTIONS用于预检请求必须包含）
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH")
                // 5. 允许的请求头（* 表示所有，若前端有自定义头如Token，可明确指定如 "Authorization, Content-Type"）
                .allowedHeaders("*")
                // 6. 暴露的响应头（前端需获取的自定义响应头需在此声明，如分页总数、Token等）
                .exposedHeaders("X-Total-Count", "Authorization", "X-Request-ID")
                // 7. 预检请求缓存时间（3600秒=1小时，减少预检请求次数，提升性能）
                .maxAge(3600);
    }
}
