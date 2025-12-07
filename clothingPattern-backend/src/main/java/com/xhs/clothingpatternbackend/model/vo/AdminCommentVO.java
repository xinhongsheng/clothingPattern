package com.xhs.clothingpatternbackend.model.vo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * @Author: 小辛同学
 * @CreateTime: 2025-12-07
 * @Description:
 * @Version: 1.0
 */
@Data
public class AdminCommentVO implements Serializable {
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
     * 评论时间
     */
    private Date createTime;

    /**
     * 用户名
     */
    private String userName;

    /**
     * 图案名称
     */
    private String patternName;
}
