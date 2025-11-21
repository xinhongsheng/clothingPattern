package com.xhs.clothingpatternbackend.model.dto.pattern;

import lombok.Data;

import java.io.Serializable;

/**
 * 图案更新请求
 */
@Data
public class PatternUpdateRequest implements Serializable {

    /**
     * 图案ID
     */
    private Long id;

    /**
     * 图案名称
     */
    private String patternName;

    /**
     * 描述
     */
    private String description;

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

    private static final long serialVersionUID = 1L;
}
