package com.xhs.clothingpatternbackend.model.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 收藏结果
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CollectResult implements Serializable {
    /**
     * 收藏状态：true-已收藏，false-未收藏
     */
    private Boolean collected;

    /**
     * 收藏总数
     */
    private Integer collectCount;

    private static final long serialVersionUID = 1L;
}

