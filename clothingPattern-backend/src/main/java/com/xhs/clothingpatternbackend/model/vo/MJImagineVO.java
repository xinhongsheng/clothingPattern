package com.xhs.clothingpatternbackend.model.vo;

import com.alibaba.fastjson2.annotation.JSONField;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @Author: 小辛同学
 * @CreateTime: 2025-11-28
 * @Description: 图片生成响应结果
 * @Version: 1.0
 */
@Data
public class MJImagineVO implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * 缩略图URL
     */
    @JSONField(name = "image_url")
    private String imageUrl;
    
    /**
     * 图片宽度
     */
    @JSONField(name = "image_width")
    private Integer imageWidth;
    
    /**
     * 图片高度
     */
    @JSONField(name = "image_height")
    private Integer imageHeight;
    
    /**
     * 可执行的动作列表
     */
    @JSONField(name = "actions")
    private List<String> actions;
    
    /**
     * 原始图片URL
     */
    @JSONField(name = "raw_image_url")
    private String rawImageUrl;
    
    /**
     * 原始图片宽度
     */
    @JSONField(name = "raw_image_width")
    private Integer rawImageWidth;
    
    /**
     * 原始图片高度
     */
    @JSONField(name = "raw_image_height")
    private Integer rawImageHeight;
    
    /**
     * 子图片URL列表
     */
    @JSONField(name = "sub_image_urls")
    private List<String> subImageUrls;
    
    /**
     * 进度（0-100）
     */
    @JSONField(name = "progress")
    private Integer progress;
    
    /**
     * 图片ID
     */
    @JSONField(name = "image_id")
    private String imageId;
    
    /**
     * 任务ID
     */
    @JSONField(name = "task_id")
    private String taskId;
    
    /**
     * 是否成功
     */
    @JSONField(name = "success")
    private Boolean success;
    
    /**
     * 追踪ID
     */
    @JSONField(name = "trace_id")
    private String traceId;
    
    /**
     * 图案名称
     */
    private String patternName;
    
    /**
     * 提示词
     */
    private String prompt;
    
    /**
     * 图案风格
     */
    private String style;
    
    /**
     * 适用季节
     */
    private String season;
    
    /**
     * 目标受众
     */
    private String targetAudience;

    /**
     * 参考图片URL（有值时为图片生成类型，否则为文字生成类型）
     */
    private String referenceImageUrl;
}

