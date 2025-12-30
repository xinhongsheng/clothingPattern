package com.xhs.clothingpatternbackend.mq;

import com.xhs.clothingpatternbackend.config.ImageFusionRabbitConfig;
import com.xhs.clothingpatternbackend.model.dto.imagefusion.ImageFusionGenerateMessage;
import com.xhs.clothingpatternbackend.model.entity.ImageFusionTask;
import com.xhs.clothingpatternbackend.service.ImageFusionTaskService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class ImageFusionGenerateConsumer {

    private static final int MAX_ATTEMPTS = 120;
    private static final long POLL_INTERVAL_MS = 3000;

    private final ImageFusionTaskService taskService;

    public ImageFusionGenerateConsumer(ImageFusionTaskService taskService) {
        this.taskService = taskService;
    }

    @RabbitListener(queues = ImageFusionRabbitConfig.IMAGE_FUSION_QUEUE)
    public void handleFusionGenerate(ImageFusionGenerateMessage message) {
        if (message == null || StringUtils.isBlank(message.getTaskId())) {
            return;
        }
        String taskId = message.getTaskId();
        try {
            for (int i = 0; i < MAX_ATTEMPTS; i++) {
                ImageFusionTask task = taskService.queryTaskStatus(taskId);
                if (task == null) {
                    return;
                }
                String status = task.getTaskStatus();
                if ("SUCCEEDED".equals(status) || "FAILED".equals(status)) {
                    return;
                }
                Thread.sleep(POLL_INTERVAL_MS);
            }
            log.warn("Image fusion task still pending after polling, taskId={}", taskId);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.warn("Image fusion polling interrupted, taskId={}", taskId, e);
        } catch (Exception e) {
            log.error("Image fusion async processing failed, taskId={}", taskId, e);
            taskService.lambdaUpdate()
                    .eq(ImageFusionTask::getDashscopeTaskId, taskId)
                    .set(ImageFusionTask::getTaskStatus, "FAILED")
                    .set(ImageFusionTask::getErrorMessage, e.getMessage())
                    .update();
        }
    }
}
