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
@AllArgsConstructor
public class PageResult<T> {
    /**
     * 数据列表
     */
    private List<T> records;
    
    /**
     * 总记录数
     */
    private Long total;
    
    /**
     * 当前页码
     */
    private Long current;
    
    /**
     * 每页大小
     */
    private Long size;
}

