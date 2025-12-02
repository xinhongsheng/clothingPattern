package com.xhs.clothingpatternbackend.model.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * @Author: 小辛同学
 * @CreateTime: 2025-12-02
 * @Description:
 * @Version: 1.0
 */
@Data
public class QueryTaskResult {
    private String taskId;
    private String taskStatus; // 包含PENDING/PRE-PROCESSING/RUNNING等状态
    private String imageUrl;   // 阿里云返回的临时图片URL
    private LocalDateTime submitTime;
    private LocalDateTime scheduledTime;
    private LocalDateTime endTime;
    private String errorCode;
    private String errorMessage;
    private Integer imageCount;
    private String requestId;
}
