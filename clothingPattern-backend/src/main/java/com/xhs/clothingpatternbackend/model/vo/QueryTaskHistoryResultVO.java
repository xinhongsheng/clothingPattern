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
public class QueryTaskHistoryResultVO {
    private Long id;                // 任务记录ID（用于删除等操作）
    private String localImageUrl;   // 阿里云返回的临时图片URL
    private LocalDateTime submitTime;
    private LocalDateTime endTime;
}
