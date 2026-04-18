package com.xhs.clothingpatternbackend.service;

import cn.hutool.json.JSONUtil;
import com.xhs.clothingpatternbackend.model.dto.mj.MJGenerateTaskInfo;
import com.xhs.clothingpatternbackend.model.enums.PatternGenerateStatusEnum;
import com.xhs.clothingpatternbackend.model.vo.MJImagineVO;
import jakarta.annotation.Resource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
public class MJGenerateTaskService {

    private static final String TASK_KEY_PREFIX = "xhs_bailian:generate_task:";
    private static final long TASK_TTL_SECONDS = 86400;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    public MJGenerateTaskInfo createTask(Long userId) {
        String taskId = UUID.randomUUID().toString().replace("-", "");
        long now = System.currentTimeMillis();
        MJGenerateTaskInfo info = new MJGenerateTaskInfo();
        info.setTaskId(taskId);
        info.setUserId(userId);
        info.setStatus(PatternGenerateStatusEnum.PENDING.getValue());
        info.setCreateTime(now);
        info.setUpdateTime(now);
        saveTask(info);
        return info;
    }

    public MJGenerateTaskInfo getTask(String taskId) {
        if (taskId == null || taskId.isBlank()) {
            return null;
        }
        String value = stringRedisTemplate.opsForValue().get(getTaskKey(taskId));
        if (value == null) {
            return null;
        }
        return JSONUtil.toBean(value, MJGenerateTaskInfo.class);
    }

    public void markProcessing(String taskId) {
        updateStatus(taskId, PatternGenerateStatusEnum.PROCESSING.getValue(), null, null);
    }

    public void markSucceeded(String taskId, MJImagineVO result) {
        updateStatus(taskId, PatternGenerateStatusEnum.SUCCEEDED.getValue(), result, null);
    }

    public void markFailed(String taskId, String errorMessage) {
        updateStatus(taskId, PatternGenerateStatusEnum.FAILED.getValue(), null, errorMessage);
    }

    private void updateStatus(String taskId, String status, MJImagineVO result, String errorMessage) {
        MJGenerateTaskInfo info = getTask(taskId);
        if (info == null) {
            return;
        }
        info.setStatus(status);
        if (result != null) {
            info.setResult(result);
        }
        if (errorMessage != null) {
            info.setErrorMessage(errorMessage);
        }
        info.setUpdateTime(System.currentTimeMillis());
        saveTask(info);
    }

    private void saveTask(MJGenerateTaskInfo info) {
        String key = getTaskKey(info.getTaskId());
        String value = JSONUtil.toJsonStr(info);
        stringRedisTemplate.opsForValue().set(key, value, TASK_TTL_SECONDS, TimeUnit.SECONDS);
    }

    private String getTaskKey(String taskId) {
        return TASK_KEY_PREFIX + taskId;
    }
}
