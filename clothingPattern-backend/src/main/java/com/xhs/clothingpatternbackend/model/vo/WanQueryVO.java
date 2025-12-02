package com.xhs.clothingpatternbackend.model.vo;

import com.alibaba.fastjson2.annotation.JSONField;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @Author: 小辛同学
 * @CreateTime: 2025-12-02
 * @Description:
 * @Version: 1.0
 */
@Data
public class WanQueryVO {
    // 1. 对齐API返回字段（request_id → requestId）
    @JSONField(name = "request_id")
    private String requestId;

    // 2. 输出核心信息（必选字段）
    private Output output;

    // 3. 用量信息（可选，可能为null）
    private Usage usage;

    /**
     * 输出详情（API返回的output节点）
     */
    @Data
    public static class Output {
        // 通义任务ID（task_id → taskId）
        @JSONField(name = "task_id")
        private String taskId;

        // 任务状态（task_status → taskStatus，如PENDING/SUCCEEDED）
        @JSONField(name = "task_status")
        private String taskStatus;

        // 日期字段：指定API返回的时间格式（避免解析失败）
        @JSONField(name = "submit_time", format = "yyyy-MM-dd HH:mm:ss.SSS")
        private LocalDateTime submitTime;

        @JSONField(name = "scheduled_time", format = "yyyy-MM-dd HH:mm:ss.SSS")
        private LocalDateTime scheduledTime;

        @JSONField(name = "end_time", format = "yyyy-MM-dd HH:mm:ss.SSS")
        private LocalDateTime endTime;

        // 结果图片列表（results → results，1-4张图）
        private List<Result> results;

        // 错误信息（失败时返回，code/message对应错误码和详情）
        private String code;
        private String message;

        // 任务指标（可选，可能为null）
        @JSONField(name = "task_metrics")
        private TaskMetrics taskMetrics;
    }

    /**
     * 单张结果图片信息（API返回的output.results节点）
     */
    @Data
    public static class Result {
        // 原始提示词（orig_prompt → origPrompt）
        @JSONField(name = "orig_prompt")
        private String origPrompt;

        // 临时图片URL（url → url，24小时有效）
        private String url;

        // 单图状态码（0表示成功，非0表示失败）
        private String code;

        // 单图错误信息（失败时返回）
        private String message;
    }

    /**
     * 任务指标（可选，如总图片数、成功数）
     */
    @Data
    public static class TaskMetrics {
        private Integer total;       // 总图片数
        private Integer succeeded;   // 成功数
        private Integer failed;      // 失败数
    }

    /**
     * 用量信息（API返回的usage节点）
     */
    @Data
    public static class Usage {
        // 图片用量（image_count → imageCount，JSON字段下划线转驼峰）
        @JSONField(name = "image_count")
        private Integer imageCount;
    }
}
