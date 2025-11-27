package com.xhs.clothingpatternbackend.model.dto.article;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

/**
 * @Author: 小辛同学
 * @CreateTime: 2025-11-26
 * @Description:
 * @Version: 1.0
 */
@Data
public class ArticleAddRequest {
    @NotNull(message = "分类ID不能为空")
    private Long categoryId;

    @NotBlank(message = "标题不能为空")
    @Size(max = 200, message = "标题不能超过200字")
    private String title;

    private String coverImage;

    @NotBlank(message = "摘要不能为空")
    @Size(max = 500, message = "摘要不能超过500字")
    private String summary;

    @NotBlank(message = "内容不能为空")
    private String content;

    private String author;
    private String source;
    private List<String> tags;

    private Integer isTop = 0;
    private Integer isRecommend = 0;
}
