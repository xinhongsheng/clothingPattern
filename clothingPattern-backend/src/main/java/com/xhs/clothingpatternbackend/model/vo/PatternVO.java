package com.xhs.clothingpatternbackend.model.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 图案视图对象
 */
@Data
public class PatternVO implements Serializable {

    /**
     * 图案ID
     */
    private Long id;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 图案名称
     */
    private String patternName;

    /**
     * 描述
     */
    private String description;

    /**
     * 生成类型
     */
    private String generationType;

    /**
     * 参考图片URL
     */
    private String referenceImageUrl;

    /**
     * 图案URL
     */
    private String patternUrl;

    /**
     * 缩略图URL
     */
    private String thumbUrl;

    /**
     * 文件大小
     */
    private Integer fileSize;

    /**
     * 文件类型
     */
    private String fileType;

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

    /**
     * 审核时间
     */
    private Date auditTime;

    /**
     * 拒绝原因
     */
    private String rejectReason;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    /**
     * 创建用户信息
     */
    private UserVO user;

    /**
     * 点赞数
     */
    private Long likeCount;

    /**
     * 当前用户是否已点赞
     */
    private Boolean isLiked;

    private static final long serialVersionUID = 1L;
}
