package com.xhs.clothingpatternbackend.model.vo;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * @Author: 小辛同学
 * @CreateTime: 2025-11-26
 * @Description: 点赞操作记录
 * @Version: 1.0
 */
@Data
@AllArgsConstructor
public class LikeOperation {
    private Long userId;
    private Long patternId;
    private boolean status;
    private LocalDateTime operationTime;
}
