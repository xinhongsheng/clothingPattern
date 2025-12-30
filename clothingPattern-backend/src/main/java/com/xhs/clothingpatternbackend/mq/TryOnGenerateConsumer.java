package com.xhs.clothingpatternbackend.mq;

import com.xhs.clothingpatternbackend.config.TryOnRabbitConfig;
import com.xhs.clothingpatternbackend.model.dto.tryon.TryOnGenerateMessage;
import com.xhs.clothingpatternbackend.model.entity.TryOnTask;
import com.xhs.clothingpatternbackend.service.TryOnTaskService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class TryOnGenerateConsumer {

    private static final int MAX_ATTEMPTS = 120;
    private static final long POLL_INTERVAL_MS = 3000;

    private final TryOnTaskService tryOnTaskService;

    public TryOnGenerateConsumer(TryOnTaskService tryOnTaskService) {
        this.tryOnTaskService = tryOnTaskService;
    }

    @RabbitListener(queues = TryOnRabbitConfig.TRY_ON_QUEUE)
    public void handleTryOnGenerate(TryOnGenerateMessage message) {
        if (message == null || StringUtils.isBlank(message.getTaskId())) {
            return;
        }
        String taskId = message.getTaskId();
        try {
            for (int i = 0; i < MAX_ATTEMPTS; i++) {
                TryOnTask task = tryOnTaskService.queryTaskStatus(taskId);
                if (task == null) {
                    return;
                }
                String status = task.getTaskStatus();
                if ("SUCCEEDED".equals(status) || "SUCCESS".equals(status) || "FAILED".equals(status)) {
                    return;
                }
                Thread.sleep(POLL_INTERVAL_MS);
            }
            log.warn("Try-on task still pending after polling, taskId={}", taskId);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.warn("Try-on polling interrupted, taskId={}", taskId, e);
        } catch (Exception e) {
            log.error("Try-on async processing failed, taskId={}", taskId, e);
            tryOnTaskService.lambdaUpdate()
                    .eq(TryOnTask::getDashscopeTaskId, taskId)
                    .set(TryOnTask::getTaskStatus, "FAILED")
                    .set(TryOnTask::getErrorMessage, e.getMessage())
                    .update();
        }
    }
}
