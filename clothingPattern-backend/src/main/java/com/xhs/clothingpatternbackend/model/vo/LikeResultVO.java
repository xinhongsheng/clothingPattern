package com.xhs.clothingpatternbackend.model.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * 点赞结果视图对象
 */
@Data
public class LikeResultVO implements Serializable {

    /**
     * 当前点赞状态（true-已点赞，false-未点赞）
     */
    private Boolean isLiked;

    /**
     * 最新点赞数
     */
    private Long likeCount;

    private static final long serialVersionUID = 1L;
}
