package com.xhs.clothingpatternbackend.model.dto.mj;

import lombok.Data;

import java.io.Serializable;

/**
 * @Author: 小辛同学
 * @CreateTime: 2025-11-28
 * @Description: 图片生成请求参数
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
    
    /**
     * 图案风格（如：简约、可爱、复古等）
     */
    private String style;
    
    /**
     * 适用季节（如：春季、夏季、秋季、冬季、四季）
     */
    private String season;
    
    /**
     * 目标受众（如：儿童、青少年、成人、中老年、通用）
     */
    private String targetAudience;
}

