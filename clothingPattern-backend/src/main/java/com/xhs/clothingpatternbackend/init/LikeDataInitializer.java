package com.xhs.clothingpatternbackend.init;

import com.xhs.clothingpatternbackend.mapper.PatternMapper;
import com.xhs.clothingpatternbackend.model.entity.Pattern;
import com.xhs.clothingpatternbackend.service.LikeService;
import com.xhs.clothingpatternbackend.task.LikeSyncTask;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @Author: 小辛同学
 * @CreateTime: 2025-11-26
 * @Description: 点赞数预热到redis
 * @Version: 1.0
 */
@Component
@Slf4j
@ConfigurationProperties(prefix = "like.warmup")
@Data
public class LikeDataInitializer implements CommandLineRunner {
    @Autowired
    private LikeService likeService;

    @Autowired
    private PatternMapper patternMapper;
    
    @Autowired
    private LikeSyncTask likeSyncTask;
    
    /**
     * 预热天数
     */
    private int days = 30;  // 默认值30

    /**
     * 预热数量限制
     */
    private int limit = 1000;  // 默认值1000

    @Override
    public void run(String... args) {
        log.info("开始预热点赞数据到Redis...");

        long startTime = System.currentTimeMillis();

        try {
            // 1. 先修复Redis中的数据类型
            log.info("步骤1: 修复Redis点赞计数数据类型...");
            likeSyncTask.fixLikeCountDataType();
            
            // 2. 预热最近活跃的图案数据
            log.info("步骤2: 预热最近活跃图案数据...");
            List<Pattern> recentPatterns = patternMapper.selectRecentActivePatterns(this.days, this.limit);

            if (recentPatterns != null && !recentPatterns.isEmpty()) {
                List<Long> patternIds = recentPatterns.stream()
                        .map(Pattern::getId)
                        .collect(Collectors.toList());

                likeService.batchWarmUpLikeData(patternIds);

                log.info("点赞数据预热完成，共预热 {} 个图案，耗时: {}ms",
                        recentPatterns.size(),
                        System.currentTimeMillis() - startTime);
            } else {
                log.info("没有找到需要预热的图案数据");
            }

        } catch (Exception e) {
            log.error("预热点赞数据失败", e);
        }
    }

    /**
     * 手动触发数据预热（可用于管理后台）
     */
    public void manualWarmUp() {
        new Thread(() -> {
            log.info("手动触发点赞数据预热...");
            this.run();
        }).start();
    }
}
