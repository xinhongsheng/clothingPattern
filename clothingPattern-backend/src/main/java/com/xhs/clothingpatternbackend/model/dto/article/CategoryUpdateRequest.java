package com.xhs.clothingpatternbackend.model.dto.article;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 更新分类请求
 */
@Data
public class CategoryUpdateRequest {
    
    @NotNull(message = "分类ID不能为空")
    private Long id;
    
    @NotBlank(message = "分类名称不能为空")
    @Size(max = 50, message = "分类名称不能超过50字")
    private String categoryName;
    
    @Size(max = 200, message = "分类描述不能超过200字")
    private String categoryDesc;
    
    /**
     * 分类图标
     */
    private String icon;
    
    /**
     * 排序字段，越大越靠前
     */
    private Integer sortOrder;
    
    /**
     * 状态：0-禁用，1-启用
     */
    private Integer status;
}
