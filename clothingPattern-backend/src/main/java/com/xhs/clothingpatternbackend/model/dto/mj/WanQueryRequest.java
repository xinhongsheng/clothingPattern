package com.xhs.clothingpatternbackend.model.dto.mj;

import com.xhs.clothingpatternbackend.common.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 融合图查询请求
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class WanQueryRequest extends PageRequest implements Serializable {
    /**
     * 融合图ID
     */
    private Long id;

    /**
     * 用户ID（精确匹配，查询当前用户的任务）
     */
    private Long userId;

    /**
     * 任务状态（精确匹配，如：PENDING/SUCCESS/FAILED/CANCELLED）
     * 对应实体类 taskStatus 字段
     */
    private String taskStatus;

    /**
     * 第三方任务ID（精确匹配，如dashscope平台任务ID）
     */
    private String dashscopeTaskId;

    /**
     * 提示词（模糊查询，匹配 prompt 或 origPrompts 字段）
     */
    private String promptKeyword;

    /**
     * 提交时间-开始（时间范围查询左边界）
     */
    private LocalDateTime startSubmitTime;

    /**
     * 提交时间-结束（时间范围查询右边界）
     */
    private LocalDateTime endSubmitTime;

    /**
     * 任务结束时间-开始（时间范围查询左边界）
     */
    private LocalDateTime startEndTime;

    /**
     * 任务结束时间-结束（时间范围查询右边界）
     */
    private LocalDateTime endEndTime;

    /**
     * 错误码（精确匹配，查询失败状态的任务时使用）
     */
    private String errorCode;

    private static final long serialVersionUID = 1L;
}
