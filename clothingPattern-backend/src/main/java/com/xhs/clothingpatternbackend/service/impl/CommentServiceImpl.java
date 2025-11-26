package com.xhs.clothingpatternbackend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xhs.clothingpatternbackend.exception.BusinessException;
import com.xhs.clothingpatternbackend.exception.ErrorCode;
import com.xhs.clothingpatternbackend.mapper.CommentLikeMapper;
import com.xhs.clothingpatternbackend.mapper.PatternMapper;
import com.xhs.clothingpatternbackend.model.dto.comment.CommentAddRequest;
import com.xhs.clothingpatternbackend.model.dto.comment.CommentQueryRequest;
import com.xhs.clothingpatternbackend.model.entity.Comment;
import com.xhs.clothingpatternbackend.model.entity.CommentLike;
import com.xhs.clothingpatternbackend.model.entity.Pattern;
import com.xhs.clothingpatternbackend.model.vo.CommentStatisticsVO;
import com.xhs.clothingpatternbackend.model.vo.CommentVO;
import com.xhs.clothingpatternbackend.model.vo.PageResult;
import com.xhs.clothingpatternbackend.service.CommentService;
import com.xhs.clothingpatternbackend.mapper.CommentMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
* @author 小辛
* @description 针对表【comment(图案评论表)】的数据库操作Service实现
* @createDate 2025-11-26 15:12:15
*/
@Service
@Slf4j
public class CommentServiceImpl extends ServiceImpl<CommentMapper, Comment>
    implements CommentService{

    @Autowired
    private CommentMapper commentMapper;

    @Autowired
    private PatternMapper patternMapper;

    @Autowired
    private CommentLikeMapper commentLikeMapper;

    @Override
    @Transactional
    public CommentVO addComment(CommentAddRequest request, Long userId) {
        // 1. 验证图案是否存在且已审核
        Pattern pattern = patternMapper.selectById(request.getPatternId());
        if (pattern == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "图案不存在");
        }
        if (!"APPROVED".equals(pattern.getAuditStatus())) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "图案未通过审核，无法评论");
        }

        // 2. 如果是回复评论，验证父评论并设置 rootId
        Long rootId = null;
        Long replyToUserId = null;
        
        if (request.getParentId() != null) {
            Comment parentComment = commentMapper.selectById(request.getParentId());
            if (parentComment == null) {
                throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "父评论不存在");
            }
            if (!parentComment.getPatternId().equals(request.getPatternId())) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "父评论不属于该图案");
            }

            // 设置 rootId：如果父评论是主评论，rootId = 父评论ID；否则 rootId = 父评论的rootId
            rootId = parentComment.getRootId() != null ? parentComment.getRootId() : parentComment.getId();
            
            // 设置被回复的用户ID
            replyToUserId = parentComment.getUserId();

            // 增加根评论的回复数
            commentMapper.incrementReplyCount(rootId, 1);
        }

        // 3. 创建评论
        Comment comment = new Comment();
        comment.setUserId(userId);
        comment.setPatternId(request.getPatternId());
        comment.setContent(request.getContent());
        comment.setParentId(request.getParentId());
        comment.setRootId(rootId);
        comment.setReplyToUserId(replyToUserId);
        comment.setLikeCount(0);
        comment.setReplyCount(0);
        comment.setTopStatus(0);
        comment.setAuditStatus("APPROVED"); // 默认通过审核，如需人工审核可改为PENDING

        commentMapper.insert(comment);

        // 4. 返回评论详情
        return getCommentDetail(comment.getId(), userId);
    }

    @Override
    public PageResult<CommentVO> getCommentList(CommentQueryRequest request, Long currentUserId) {
        // 验证图案是否存在
        Pattern pattern = patternMapper.selectById(request.getPatternId());
        if (pattern == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "图案不存在");
        }

        long current = request.getCurrent();
        long size = request.getPageSize();
        long offset = (current - 1) * size;

        // 查询主评论列表
        List<Comment> comments = commentMapper.selectCommentListByPattern(
                request.getPatternId(), offset, size);

        // 查询总数
        Integer total = commentMapper.selectMainCommentCountByPattern(request.getPatternId());

        // 转换为VO并填充回复
        List<CommentVO> commentVOList = comments.stream()
                .map(comment -> {
                    CommentVO vo = convertToVO(comment);
                    // 查询所有回复（根据 rootId）
                    List<Comment> replies = commentMapper.selectRepliesByRootId(comment.getId());
                    if (replies != null && !replies.isEmpty()) {
                        // 只返回前3条回复用于预览
                        List<CommentVO> replyVOList = replies.stream()
                                .limit(3)
                                .map(this::convertToVO)
                                .collect(Collectors.toList());
                        vo.setChildren(replyVOList);
                    }
                    return vo;
                })
                .collect(Collectors.toList());

        // 填充点赞状态
        if (currentUserId != null && !commentVOList.isEmpty()) {
            fillLikeStatus(commentVOList, currentUserId);
        }

        return new PageResult<>(commentVOList, total.longValue());
    }

    @Override
    public CommentVO getCommentDetail(Long commentId, Long currentUserId) {
        Comment comment = commentMapper.selectById(commentId);
        if (comment == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "评论不存在");
        }

        CommentVO vo = convertToVO(comment);

        // 填充点赞状态
        if (currentUserId != null) {
            Long likeCount = commentLikeMapper.countUserLike(currentUserId, commentId);
            vo.setLiked(likeCount > 0);
        }

        return vo;
    }

    @Override
    @Transactional
    public boolean deleteComment(Long commentId, Long userId) {
        Comment comment = commentMapper.selectById(commentId);
        if (comment == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "评论不存在");
        }

        // 只能删除自己的评论
        if (!comment.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "无权删除该评论");
        }

        // 如果是回复，减少根评论的回复数
        if (comment.getRootId() != null) {
            commentMapper.incrementReplyCount(comment.getRootId(), -1);
        }

        // 逻辑删除
        return commentMapper.deleteById(commentId) > 0;
    }

    @Override
    public CommentStatisticsVO getCommentStatistics(Long patternId) {
        CommentStatisticsVO statistics = new CommentStatisticsVO();
        statistics.setTotalComments(commentMapper.selectCommentCountByPattern(patternId));
        statistics.setMainComments(commentMapper.selectMainCommentCountByPattern(patternId));
        statistics.setTotalLikes(commentMapper.selectTotalLikesByPattern(patternId));
        return statistics;
    }

    @Override
    @Transactional
    public boolean toggleCommentLike(Long commentId, Long userId) {
        Comment comment = commentMapper.selectById(commentId);
        if (comment == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "评论不存在");
        }

        // 查询是否已点赞
        QueryWrapper<CommentLike> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userId", userId)
                   .eq("commentId", commentId);
        CommentLike existingLike = commentLikeMapper.selectOne(queryWrapper);

        if (existingLike != null && existingLike.getIsDelete() == 0) {
            // 已点赞，取消点赞
            existingLike.setIsDelete(1);
            commentLikeMapper.updateById(existingLike);
            commentMapper.incrementLikeCount(commentId, -1);
            return false;
        } else if (existingLike != null && existingLike.getIsDelete() == 1) {
            // 之前点赞过但已取消，恢复点赞
            existingLike.setIsDelete(0);
            commentLikeMapper.updateById(existingLike);
            commentMapper.incrementLikeCount(commentId, 1);
            return true;
        } else {
            // 首次点赞
            CommentLike commentLike = new CommentLike();
            commentLike.setUserId(userId);
            commentLike.setCommentId(commentId);
            commentLike.setPatternId(comment.getPatternId());
            commentLike.setIsDelete(0);
            commentLikeMapper.insert(commentLike);
            commentMapper.incrementLikeCount(commentId, 1);
            return true;
        }
    }

    /**
     * 转换为VO
     */
    private CommentVO convertToVO(Comment comment) {
        CommentVO vo = new CommentVO();
        BeanUtils.copyProperties(comment, vo);
        
        // 转换时间格式
        if (comment.getCreateTime() != null) {
            vo.setCreateTime(comment.getCreateTime().toInstant()
                    .atZone(ZoneId.systemDefault())
                    .toLocalDateTime());
        }
        
        // 设置用户信息（已经在 SQL 查询中通过 JOIN 获取）
        vo.setUserName(comment.getUserName());
        vo.setUserAvatar(comment.getUserAvatar());
        vo.setReplyToUserName(comment.getReplyToUserName());
        
        return vo;
    }

    /**
     * 填充点赞状态
     */
    private void fillLikeStatus(List<CommentVO> commentVOList, Long userId) {
        // 收集所有评论ID（包括回复）
        List<Long> allCommentIds = new ArrayList<>();
        for (CommentVO vo : commentVOList) {
            allCommentIds.add(vo.getId());
            if (vo.getChildren() != null) {
                allCommentIds.addAll(vo.getChildren().stream()
                        .map(CommentVO::getId)
                        .collect(Collectors.toList()));
            }
        }

        // 批量查询点赞状态
        List<Long> likedCommentIds = commentLikeMapper.selectLikedCommentIds(userId, allCommentIds);
        Set<Long> likedSet = likedCommentIds.stream().collect(Collectors.toSet());

        // 设置点赞状态
        for (CommentVO vo : commentVOList) {
            vo.setLiked(likedSet.contains(vo.getId()));
            if (vo.getChildren() != null) {
                vo.getChildren().forEach(child -> 
                    child.setLiked(likedSet.contains(child.getId())));
            }
        }
    }

    @Override
    public List<CommentVO> getCommentReplies(Long commentId, Long currentUserId) {
        // 1. 验证评论是否存在
        Comment comment = commentMapper.selectById(commentId);
        if (comment == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "评论不存在");
        }

        // 2. 查询所有回复（根据 rootId）
        List<Comment> replies = commentMapper.selectRepliesByRootId(commentId);
        
        // 3. 转换为VO
        List<CommentVO> replyVOList = replies.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());

        // 4. 填充点赞状态
        if (currentUserId != null && !replyVOList.isEmpty()) {
            List<Long> replyIds = replyVOList.stream()
                    .map(CommentVO::getId)
                    .collect(Collectors.toList());
            List<Long> likedCommentIds = commentLikeMapper.selectLikedCommentIds(currentUserId, replyIds);
            Set<Long> likedSet = likedCommentIds.stream().collect(Collectors.toSet());
            replyVOList.forEach(vo -> vo.setLiked(likedSet.contains(vo.getId())));
        }

        return replyVOList;
    }
}




