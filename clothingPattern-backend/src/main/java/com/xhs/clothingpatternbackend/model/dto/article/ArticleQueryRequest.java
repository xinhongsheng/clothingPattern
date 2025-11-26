package com.xhs.clothingpatternbackend.model.dto.article;

import lombok.Data;

import java.util.List;

/**
 * @Author: 小辛同学
 * @CreateTime: 2025-11-26
 * @Description:
 * @Version: 1.0
 */
@Data
public class ArticleQueryRequest {
    private Long categoryId;
    private String keyword;
    private List<String> tag; //使用列表存储，后面使用JSONUtil.toJsonStr转换为JSON字符串存储到mysql
    private String status = "PUBLISHED";
    private String auditStatus = "APPROVED";
    private Integer isTop;
    private Integer isHot;
    private Integer isRecommend;

    private Integer pageNum = 1;
    private Integer pageSize = 10;

    // 排序字段：publishTime, viewCount, likeCount, createTime
    private String sortField = "publishTime";
    // 排序方式：asc, desc
    private String sortOrder = "desc";
}
