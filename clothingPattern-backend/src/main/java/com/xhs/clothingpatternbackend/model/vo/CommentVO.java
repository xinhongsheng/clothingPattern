package com.xhs.clothingpatternbackend.model.vo;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @Author: 小辛同学
 * @CreateTime: 2025-11-26
 * @Description:
 * @Version: 1.0
 */
@Data
public class CommentVO {
    private Long id;
    private Long userId;
    private Long patternId;
    private String content;
    private Long parentId;
    private Long rootId;
    private Long replyToUserId;
    private Integer likeCount;
    private Integer replyCount;
    private Integer topStatus;
    private String auditStatus;
    private LocalDateTime createTime;

    private String userName;
    private String userAvatar;
    private String replyToUserName;  // 被回复用户的名称
    private Boolean liked;
    private List<CommentVO> children;
}
