package com.xhs.clothingpatternbackend.model.dto.pattern;

import com.xhs.clothingpatternbackend.common.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * 图案查询请求
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class PatternQueryRequest extends PageRequest implements Serializable {

    /**
     * 图案ID
     */
    private Long id;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 图案名称（模糊搜索）
     */
    private String patternName;

    /**
     * 生成类型
     */
    private String generationType;

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
     * 审核状态
     */
    private String auditStatus;

    private static final long serialVersionUID = 1L;
}
