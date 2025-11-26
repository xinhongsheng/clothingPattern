package com.xhs.clothingpatternbackend.model.vo;

import lombok.Data;

/**
 * 评论统计信息VO
 */
@Data
public class CommentStatisticsVO {
    /**
     * 总评论数（包括主评论和回复）
     */
    private Integer totalComments;
    
    /**
     * 主评论数（不包括回复）
     */
    private Integer mainComments;
    
    /**
     * 总点赞数
     */
    private Integer totalLikes;
}

