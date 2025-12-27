package com.xhs.clothingpatternbackend.mq;

import com.xhs.clothingpatternbackend.config.PatternGenerateRabbitConfig;
import com.xhs.clothingpatternbackend.controller.PatternController;
import com.xhs.clothingpatternbackend.model.dto.pattern.PatternGenerateMessage;
import com.xhs.clothingpatternbackend.model.entity.User;
import com.xhs.clothingpatternbackend.service.PatternGenerateTaskService;
import com.xhs.clothingpatternbackend.service.PatternService;
import com.xhs.clothingpatternbackend.service.UserService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
@Slf4j
public class PatternGenerateConsumer {

    @Resource
    private PatternGenerateTaskService taskService;

    @Resource
    private PatternService patternService;

    @Resource
    private UserService userService;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @RabbitListener(queues = PatternGenerateRabbitConfig.PATTERN_GENERATE_QUEUE)
    public void handlePatternGenerate(PatternGenerateMessage message) {
        if (message == null || message.getTaskId() == null) {
            return;
        }
        String taskId = message.getTaskId();
        taskService.markProcessing(taskId);
        try {
            if (message.getUserId() == null || message.getRequest() == null) {
                taskService.markFailed(taskId, "Invalid task payload");
                return;
            }
            User loginUser = userService.getById(message.getUserId());
            if (loginUser == null) {
                taskService.markFailed(taskId, "User not found");
                return;
            }
            Long patternId = patternService.generatePattern(message.getRequest(), loginUser);
            taskService.markSucceeded(taskId, patternId);
            clearPatternListCache();
        } catch (Exception e) {
            log.error("Pattern generation failed, taskId={}", taskId, e);
            taskService.markFailed(taskId, e.getMessage());
        }
    }

    private void clearPatternListCache() {
        try {
            PatternController.LOCAL_CACHE.invalidateAll();
            Set<String> keys = stringRedisTemplate.keys("xhs_pattern:listPictureVOByPage:*");
            if (keys != null && !keys.isEmpty()) {
                stringRedisTemplate.delete(keys);
            }
        } catch (Exception e) {
            log.warn("Failed to clear pattern list cache after async generate", e);
        }
    }
}
