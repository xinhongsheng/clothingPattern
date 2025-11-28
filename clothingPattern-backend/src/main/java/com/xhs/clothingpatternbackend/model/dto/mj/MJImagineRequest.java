package com.xhs.clothingpatternbackend.model.dto.mj;

import lombok.Data;

import java.io.Serializable;

/**
 * @Author: 小辛同学
 * @CreateTime: 2025-11-28
 * @Description: Midjourney Imagine请求参数
 * @Version: 1.0
 */
@Data
public class MJImagineRequest implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * 提示词
     */
    private String prompt;
    
    /**
     * 动作类型，默认为generate
     */
    private String action = "generate";
}

