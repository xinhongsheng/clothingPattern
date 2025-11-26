package com.xhs.clothingpatternbackend.model.entity;

import com.baomidou.mybatisplus.annotation.*;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

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
     * 根评论ID：null-主评论，非null-所有回复都指向根评论
     */
    private Long rootId;

    /**
     * 被回复的用户ID：用于显示 @用户名
     */
    private Long replyToUserId;

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

    /**
     * 点赞数
     */
    private Integer likeCount;

    /**
     * 回复数
     */
    private Integer replyCount;

    /**
     * 置顶状态：0-否，1-是
     */
    private Integer topStatus;

    /**
     * 审核状态：PENDING-待审核，APPROVED-已通过，REJECTED-已拒绝
     */
    private String auditStatus;

    // 非数据库字段
    @TableField(exist = false)
    private String userName;

    @TableField(exist = false)
    private String userAvatar;

    @TableField(exist = false)
    private String replyToUserName;

    @TableField(exist = false)
    private List<Comment> children;

    @TableField(exist = false)
    private Boolean liked;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}