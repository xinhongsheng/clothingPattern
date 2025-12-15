package com.xhs.clothingpatternbackend.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 用户行为记录表
 * @TableName user_behavior
 */
@Data
@TableName("user_behavior")
public class UserBehavior implements Serializable {

    /**
     * 主键
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 图案ID
     */
    private Long patternId;

    /**
     * 行为类型: VIEW(浏览), LIKE(点赞), DOWNLOAD(下载)
     */
    private String actionType;

    /**
     * 权重: 浏览=1, 下载=3, 点赞=5
     */
    private Integer weight;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    private static final long serialVersionUID = 1L;
}
