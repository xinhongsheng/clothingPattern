package com.xhs.clothingpatternbackend.model.dto.comment;

import com.xhs.clothingpatternbackend.common.PageRequest;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;

/**
 * @Author: 小辛同学
 * @CreateTime: 2025-11-26
 * @Description:
 * @Version: 1.0
 */
@Data
public class CommentQueryRequest extends PageRequest implements Serializable {
    @NotNull(message = "图案ID不能为空")
    private Long patternId;

    private Long parentId; // 查询指定父评论的回复

    private static final long serialVersionUID = 1L;
}
