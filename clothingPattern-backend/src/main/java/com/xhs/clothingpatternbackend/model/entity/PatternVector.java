package com.xhs.clothingpatternbackend.model.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * 图案AI特征向量表
 * @TableName pattern_vector
 */
@TableName(value ="pattern_vector")
@Data
public class PatternVector implements Serializable {
    /**
     * 关联 pattern 表的主键ID
     */
    @TableId
    private Long patternId;

    /**
     * 384维特征向量数组，存储格式为 [0.1, 0.2, ...]
     */
    private Object vectorData;

    /**
     * 创建时间
     */
    private Date createTime;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}