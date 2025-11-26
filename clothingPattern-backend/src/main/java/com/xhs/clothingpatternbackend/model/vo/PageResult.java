package com.xhs.clothingpatternbackend.model.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 分页结果VO
 */
@Data
@NoArgsConstructor
public class PageResult<T> {
    /**
     * 数据列表
     */
    private List<T> list;
    
    /**
     * 总记录数
     */
    private long total;
    
    /**
     * 便捷构造函数（只需要list和total）
     */
    public PageResult(List<T> list, long total) {
        this.list = list;
        this.total = total;
    }
}

