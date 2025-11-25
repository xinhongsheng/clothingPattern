package com.xhs.clothingpatternbackend.model.entity;

import com.baomidou.mybatisplus.annotation.*;

import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * @TableName pattern
 */
@TableName(value ="pattern")
@Data
public class Pattern implements Serializable {
    /**
     * 主键
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 用户id
     */
    private Long userId;

    /**
     * 模型名称
     */
    private String patternName;

    /**
     * 描述
     */
    private String description;

    /**
     * 生成方式
     */
    private String generationType;


    /**
     * 参考图片url
     */
    private String referenceImageUrl;

    /**
     * 模型图片url
     */
    private String patternUrl;
    /**
     * 缩略图url
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
     * 目标人群
     */
    private String targetAudience;
    /**
     * 生成参数
     */
    private Object generationParams;
    /**
     * 审核状态
     */
    private String auditStatus;
    /**
     * 审核时间
     */
    private Date auditTime;
    /**
     * 审核人id
     */
    private Long auditorId;
    /**
     * 拒绝理由
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
    @TableLogic
    private Integer isDelete;
    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}