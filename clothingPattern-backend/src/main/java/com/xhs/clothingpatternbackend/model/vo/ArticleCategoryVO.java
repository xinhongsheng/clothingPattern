package com.xhs.clothingpatternbackend.model.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

/**
 * @Author: 小辛同学
 * @CreateTime: 2025-11-27
 * @Description:
 * @Version: 1.0
 */
@Data
public class ArticleCategoryVO {
    /**
     * 分类ID（前端可能用于编辑/删除操作）
     */
    private Long id;

    /**
     * 分类名称
     */
    private String categoryName;

    /**
     * 分类描述
     */
    private String categoryDesc;

    /**
     * 分类图标（前端展示图标URL）
     */
    private String icon;

    /**
     * 排序字段（越大越靠前，前端可展示排序优先级）
     */
    private Integer sortOrder;

    /**
     * 状态：0-禁用，1-启用（前端可显示为“禁用”/“启用”文本）
     */
    private Integer status;

    /**
     * 创建时间（格式化后返回，前端直接展示）
     */
    private Date createTime;

    /**
     * 更新时间（可选，根据前端需求决定是否返回）
     */
    private Date updateTime;
}
