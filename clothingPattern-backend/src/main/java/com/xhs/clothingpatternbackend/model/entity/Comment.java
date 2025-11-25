package com.xhs.clothingpatternbackend.model.entity;

import com.baomidou.mybatisplus.annotation.*;

import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * 图案评论表
 * @TableName comment
 */
@TableName(value ="comment")
@Data
public class Comment implements Serializable {
    /**
     * 评论ID
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 评论用户ID（关联user表）
     */
    private Long userId;

    /**
     * 被评论图案ID（关联pattern表）
     */
    private Long patternId;

    /**
     * 评论内容
     */
    private String content;

    /**
     * 父评论ID：null-主评论，非null-回复某条评论
     */
    private Long parentId;

    /**
     * 评论时间
     */
    private Date createTime;

    /**
     * 更新时间（编辑评论时更新）
     */
    private Date updateTime;

    /**
     * 逻辑删除：0-未删除，1-已删除
     */
    @TableLogic
    private Integer isDelete;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}