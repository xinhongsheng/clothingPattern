package com.xhs.clothingpatternbackend.model.dto.mj;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @Author: 小辛同学
 * @CreateTime: 2025-11-28
 * @Description: 图片融合请求参数
 * @Version: 1.0
 */
@Data
public class MJBlendRequest implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * 图片URL列表（2-5张图片）
     */
    private List<String> imageUrls;
    
    /**
     * 动作类型，blend固定为"blend"
     */
    private String action = "blend";
    
    /**
     * 图案风格（可选）
     */
    private String style;
    
    /**
     * 适用季节（可选）
     */
    private String season;
    
    /**
     * 目标受众（可选）
     */
    private String targetAudience;
}

