package com.xhs.clothingpatternbackend.model.entity;

import com.baomidou.mybatisplus.annotation.*;

import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * 图案点赞表
 * @TableName like
 */
@TableName(value ="user_like")
@Data
public class UserLike implements Serializable {
    /**
     * 点赞记录ID
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 点赞用户ID（关联user表）
     */
    private Long userId;

    /**
     * 被点赞图案ID（关联pattern表）
     */
    private Long patternId;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 逻辑删除：0-未删除（有效），1-已删除
     */
    @TableLogic
    private Integer isDelete;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}