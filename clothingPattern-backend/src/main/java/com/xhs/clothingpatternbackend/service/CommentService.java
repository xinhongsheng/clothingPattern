package com.xhs.clothingpatternbackend.service;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.xhs.clothingpatternbackend.model.dto.comment.CommentAddRequest;
import com.xhs.clothingpatternbackend.model.dto.comment.CommentQueryRequest;
import com.xhs.clothingpatternbackend.model.entity.Comment;
import com.xhs.clothingpatternbackend.model.vo.AdminCommentVO;
import com.xhs.clothingpatternbackend.model.vo.CommentStatisticsVO;
import com.xhs.clothingpatternbackend.model.vo.CommentVO;
import com.xhs.clothingpatternbackend.model.vo.PageResult;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
* @author 小辛
* @description 针对表【comment(图案评论表)】的数据库操作Service
* @createDate 2025-11-26 15:12:15
*/
public interface CommentService extends IService<Comment> {

    /**
     * 添加评论
     */
    CommentVO addComment(CommentAddRequest request, Long userId);

    /**
     * 获取评论列表（分页）
     */
    PageResult<CommentVO> getCommentList(CommentQueryRequest request, Long currentUserId);

    /**
     * 获取评论详情
     */
    CommentVO getCommentDetail(Long commentId, Long currentUserId);

    /**
     * 删除评论
     */
    boolean deleteComment(Long commentId, Long userId);

    /**
     * 获取图案评论统计信息
     */
    CommentStatisticsVO getCommentStatistics(Long patternId);

    /**
     * 点赞/取消点赞评论
     */
    boolean toggleCommentLike(Long commentId, Long userId);

    /**
     * 获取评论的所有回复
     */
    java.util.List<CommentVO> getCommentReplies(Long commentId, Long currentUserId);

    /**
     * 获取查询条件
     */
    QueryWrapper<Comment> getQueryWrapper(CommentQueryRequest commentQueryRequest);

    /**
     * 获取脱敏后的评论列表
     */
    List<AdminCommentVO> getCommentVOList(List<Comment> commentList);
}
