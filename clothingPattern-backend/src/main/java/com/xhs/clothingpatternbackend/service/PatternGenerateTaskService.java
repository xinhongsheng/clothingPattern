package com.xhs.clothingpatternbackend.service;

import cn.hutool.json.JSONUtil;
import com.xhs.clothingpatternbackend.model.dto.pattern.PatternGenerateTaskInfo;
import com.xhs.clothingpatternbackend.model.enums.PatternGenerateStatusEnum;
import jakarta.annotation.Resource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
public class PatternGenerateTaskService {

    private static final String TASK_KEY_PREFIX = "xhs_pattern:generate_task:";
    private static final long TASK_TTL_SECONDS = 86400;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    public PatternGenerateTaskInfo createTask(Long userId) {
        String taskId = UUID.randomUUID().toString().replace("-", "");
        long now = System.currentTimeMillis();
        PatternGenerateTaskInfo info = new PatternGenerateTaskInfo();
        info.setTaskId(taskId);
        info.setUserId(userId);
        info.setStatus(PatternGenerateStatusEnum.PENDING.getValue());
        info.setCreateTime(now);
        info.setUpdateTime(now);
        saveTask(info);
        return info;
    }

    public PatternGenerateTaskInfo getTask(String taskId) {
        if (taskId == null || taskId.isBlank()) {
            return null;
        }
        String value = stringRedisTemplate.opsForValue().get(getTaskKey(taskId));
        if (value == null) {
            return null;
        }
        return JSONUtil.toBean(value, PatternGenerateTaskInfo.class);
    }

    public void markProcessing(String taskId) {
        updateStatus(taskId, PatternGenerateStatusEnum.PROCESSING.getValue(), null, null);
    }

    public void markSucceeded(String taskId, Long patternId) {
        updateStatus(taskId, PatternGenerateStatusEnum.SUCCEEDED.getValue(), patternId, null);
    }

    public void markFailed(String taskId, String errorMessage) {
        updateStatus(taskId, PatternGenerateStatusEnum.FAILED.getValue(), null, errorMessage);
    }

    private void updateStatus(String taskId, String status, Long patternId, String errorMessage) {
        PatternGenerateTaskInfo info = getTask(taskId);
        if (info == null) {
            return;
        }
        info.setStatus(status);
        if (patternId != null) {
            info.setPatternId(patternId);
        }
        if (errorMessage != null) {
            info.setErrorMessage(errorMessage);
        }
        info.setUpdateTime(System.currentTimeMillis());
        saveTask(info);
    }

    private void saveTask(PatternGenerateTaskInfo info) {
        String key = getTaskKey(info.getTaskId());
        String value = JSONUtil.toJsonStr(info);
        stringRedisTemplate.opsForValue().set(key, value, TASK_TTL_SECONDS, TimeUnit.SECONDS);
    }

    private String getTaskKey(String taskId) {
        return TASK_KEY_PREFIX + taskId;
    }
}
