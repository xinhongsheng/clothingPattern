package com.xhs.clothingpatternbackend.mq;

import com.xhs.clothingpatternbackend.config.MJGenerateRabbitConfig;
import com.xhs.clothingpatternbackend.model.dto.mj.MJGenerateMessage;
import com.xhs.clothingpatternbackend.model.vo.MJImagineVO;
import com.xhs.clothingpatternbackend.sdk.dashscope.BailianImageClient;
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
    private final BailianImageClient bailianImageClient;
    private final PromptTranslateService promptTranslateService;

    public MJGenerateConsumer(MJGenerateTaskService taskService,
                              BailianImageClient bailianImageClient,
                              PromptTranslateService promptTranslateService) {
        this.taskService = taskService;
        this.bailianImageClient = bailianImageClient;
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

            MJImagineVO response = bailianImageClient.imagine(message.getRequest());
            if (response == null || !Boolean.TRUE.equals(response.getSuccess())) {
                taskService.markFailed(taskId, "Image generation failed");
                return;
            }
            response.setTaskId(taskId);
            taskService.markSucceeded(taskId, response);
        } catch (Exception e) {
            log.error("Bailian image generation failed, taskId={}", taskId, e);
            taskService.markFailed(taskId, e.getMessage());
        }
    }
}
