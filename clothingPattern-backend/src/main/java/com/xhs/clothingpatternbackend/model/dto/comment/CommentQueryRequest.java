package com.xhs.clothingpatternbackend.model.dto.comment;

import com.xhs.clothingpatternbackend.common.PageRequest;
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
    private Long patternId;

    private Long parentId; // 查询指定父评论的回复

    private String userName; // 按用户名搜索（模糊）

    private String patternName; // 按图案名搜索（模糊）

    private String content; // 按评论内容搜索（模糊）

    private static final long serialVersionUID = 1L;
}
