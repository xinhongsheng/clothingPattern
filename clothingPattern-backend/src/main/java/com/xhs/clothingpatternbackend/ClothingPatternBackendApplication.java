package com.xhs.clothingpatternbackend;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;

@SpringBootApplication
@MapperScan("com.xhs.clothingpatternbackend.mapper")
@EnableRedisHttpSession(maxInactiveIntervalInSeconds = 2592000) // 30天超时
@EnableScheduling // 启用定时任务
public class ClothingPatternBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(ClothingPatternBackendApplication.class, args);
    }

}
