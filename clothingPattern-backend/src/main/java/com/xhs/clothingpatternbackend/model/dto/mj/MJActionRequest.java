package com.xhs.clothingpatternbackend.model.dto.mj;

import com.xhs.clothingpatternbackend.model.vo.MJImagineVO;
import lombok.Data;

import java.io.Serializable;

/**
 * @Author: 小辛同学
 * @CreateTime: 2025-11-28
 * @Description: 图片生成动作请求参数
 * @Version: 1.0
 */
@Data
public class MJActionRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 任务ID（可选，如果传递了sourceResult则不需要）
     */
    private String taskId;

    /**
     * 图片ID
     */
    private String imageId;

    /**
     * 动作类型：upsample1-4, variation1-4, reroll等
     */
    private String action;

    /**
     * 原始生成结果（可选，如果没有taskId可以直接传递原始结果）
     * 用于同步生成场景，前端保存完整的生成结果，执行变体时直接传递
     */
    private MJImagineVO sourceResult;
}

