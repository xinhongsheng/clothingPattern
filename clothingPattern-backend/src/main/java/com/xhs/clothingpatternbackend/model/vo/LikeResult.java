package com.xhs.clothingpatternbackend.model.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 点赞结果
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LikeResult implements Serializable {
    /**
     * 点赞状态：true-已点赞，false-未点赞
     */
    private Boolean liked;

    /**
     * 点赞总数
     */
    private Long likeCount;

    private static final long serialVersionUID = 1L;
}

