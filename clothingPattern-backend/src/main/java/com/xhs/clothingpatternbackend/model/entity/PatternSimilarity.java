package com.xhs.clothingpatternbackend.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 图案相似度矩阵表
 * @TableName pattern_similarity
 */
@Data
@TableName("pattern_similarity")
public class PatternSimilarity implements Serializable {

    /**
     * 主键
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 图案A的ID
     */
    private Long patternIdA;

    /**
     * 图案B的ID
     */
    private Long patternIdB;

    /**
     * 相似度分数 (0~1)
     */
    private Double similarity;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

    private static final long serialVersionUID = 1L;
}
