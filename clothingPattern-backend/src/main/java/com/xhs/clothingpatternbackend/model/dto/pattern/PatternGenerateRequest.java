package com.xhs.clothingpatternbackend.model.dto.pattern;

import lombok.Data;

import java.io.Serializable;

/**
 * 图案生成请求
 */
@Data
public class PatternGenerateRequest implements Serializable {

    /**
     * AI服务类型：qwen、doubao
     */
    private String serviceType;

    /**
     * 豆包生成模式：single_text, single_image, multi_image, batch_text, batch_single_image, batch_multi_image
     */
    private String doubaoMode;

    /**
     * 多图参考URL列表（多图生图时使用）
     */
    private java.util.List<String> referenceImageUrls;

    /**
     * 批量生成图片数量（组图模式时使用）
     */
    private Integer maxImages;

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
