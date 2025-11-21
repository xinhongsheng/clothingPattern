package com.xhs.clothingpatternbackend.model.dto.pattern;

import lombok.Data;

import java.io.Serializable;

/**
 * 图案生成请求
 */
@Data
public class PatternGenerateRequest implements Serializable {

    /**
     * 图案名称
     */
    private String patternName;

    /**
     * 描述（文字生成时使用）
     */
    private String description;

    /**
     * 生成类型：TEXT_GENERATED、IMAGE_REFERENCED
     */
    private String generationType;

    /**
     * 参考图片URL（图片参考生成时使用）
     */
    private String referenceImageUrl;

    /**
     * 风格
     */
    private String style;

    /**
     * 季节
     */
    private String season;

    /**
     * 目标受众
     */
    private String targetAudience;

    /**
     * 图片尺寸（如：1024*1024）
     */
    private String size;

    /**
     * 负面提示词
     */
    private String negativePrompt;

    /**
     * 是否扩展提示词
     */
    private Boolean promptExtend;

    private static final long serialVersionUID = 1L;
}
