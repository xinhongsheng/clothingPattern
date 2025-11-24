package com.xhs.clothingpatternbackend.model.dto.pattern;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @Author: 小辛同学
 * @CreateTime: 2025-11-22
 * @Description:
 * @Version: 1.0
 */
@Data
public class PatternEditRequest implements Serializable {
    /**
     * id
     */
    private Long id;

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
