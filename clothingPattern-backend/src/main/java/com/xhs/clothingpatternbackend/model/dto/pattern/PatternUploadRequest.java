package com.xhs.clothingpatternbackend.model.dto.pattern;

import lombok.Data;

import java.io.Serializable;

/**
 * 图案上传请求
 */
@Data
public class PatternUploadRequest implements Serializable {
    /**
     * 名称
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
     * 目标人群
     */
    private String targetAudience;

    private static final long serialVersionUID = 1L;
}
