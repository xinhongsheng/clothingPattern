package com.xhs.clothingpatternbackend.mq;

import com.xhs.clothingpatternbackend.config.MJGenerateRabbitConfig;
import com.xhs.clothingpatternbackend.model.dto.mj.MJGenerateMessage;
import com.xhs.clothingpatternbackend.model.vo.MJImagineVO;
import com.xhs.clothingpatternbackend.sdk.mj.MJGenImage;
import com.xhs.clothingpatternbackend.service.MJGenerateTaskService;
import com.xhs.clothingpatternbackend.service.PromptTranslateService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class MJGenerateConsumer {

    private final MJGenerateTaskService taskService;
    private final MJGenImage mjGenImage;
    private final PromptTranslateService promptTranslateService;

    public MJGenerateConsumer(MJGenerateTaskService taskService,
                              MJGenImage mjGenImage,
                              PromptTranslateService promptTranslateService) {
        this.taskService = taskService;
        this.mjGenImage = mjGenImage;
        this.promptTranslateService = promptTranslateService;
    }

    @RabbitListener(queues = MJGenerateRabbitConfig.MJ_GENERATE_QUEUE)
    public void handleMjGenerate(MJGenerateMessage message) {
        if (message == null || StringUtils.isBlank(message.getTaskId())) {
            return;
        }
        String taskId = message.getTaskId();
        taskService.markProcessing(taskId);
        try {
            if (message.getUserId() == null || message.getRequest() == null) {
                taskService.markFailed(taskId, "Invalid task payload");
                return;
            }
            String originalPrompt = message.getRequest().getPrompt();
            if (StringUtils.isBlank(originalPrompt)) {
                taskService.markFailed(taskId, "Prompt is empty");
                return;
            }

            String optimizedPrompt = promptTranslateService.translateAndOptimize(
                    originalPrompt,
                    message.getRequest().getStyle(),
                    message.getRequest().getSeason(),
                    message.getRequest().getTargetAudience()
            );
            message.getRequest().setPrompt(optimizedPrompt);

            MJImagineVO response = mjGenImage.imagine(message.getRequest());
            if (response == null || !Boolean.TRUE.equals(response.getSuccess())) {
                taskService.markFailed(taskId, "Image generation failed");
                return;
            }
            taskService.markSucceeded(taskId, response);
        } catch (Exception e) {
            log.error("MJ generation failed, taskId={}", taskId, e);
            taskService.markFailed(taskId, e.getMessage());
        }
    }
}
