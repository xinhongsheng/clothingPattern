package com.xhs.clothingpatternbackend.model.entity;

import com.baomidou.mybatisplus.annotation.*;

import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * 评论点赞表
 * @TableName comment_like
 */
@TableName(value ="comment_like")
@Data
public class CommentLike implements Serializable {
    /**
     * 评论点赞ID
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 点赞用户ID
     */
    private Long userId;

    /**
     * 被点赞评论ID
     */
    private Long commentId;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 添加图案ID
     */
    private Long patternId; // 添加图案ID冗余字段

    /**
     * 逻辑删除
     */
    @TableLogic
    private Integer isDelete;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}