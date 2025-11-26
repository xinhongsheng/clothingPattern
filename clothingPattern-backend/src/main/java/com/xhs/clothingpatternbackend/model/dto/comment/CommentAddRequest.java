package com.xhs.clothingpatternbackend.model.dto.comment;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * @Author: 小辛同学
 * @CreateTime: 2025-11-26
 * @Description: 添加评论
 * @Version: 1.0
 */
@Data
public class CommentAddRequest {
    @NotNull(message = "图案ID不能为空")
    private Long patternId;

    @NotBlank(message = "评论内容不能为空")
    @Size(max = 500, message = "评论内容不能超过500字")
    private String content;

    private Long parentId; // 父评论ID，为空表示主评论
}
