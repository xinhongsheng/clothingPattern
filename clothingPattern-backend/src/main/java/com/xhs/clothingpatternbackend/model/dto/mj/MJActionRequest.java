package com.xhs.clothingpatternbackend.model.dto.mj;

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
     * 任务ID
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
}

