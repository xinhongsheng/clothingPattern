package com.xhs.clothingpatternbackend.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xhs.clothingpatternbackend.annotation.AuthCheck;
import com.xhs.clothingpatternbackend.common.BaseResponse;
import com.xhs.clothingpatternbackend.common.DeleteRequest;
import com.xhs.clothingpatternbackend.common.ResultUtils;
import com.xhs.clothingpatternbackend.constant.UserConstant;
import com.xhs.clothingpatternbackend.exception.ErrorCode;
import com.xhs.clothingpatternbackend.exception.ThrowUtils;
import com.xhs.clothingpatternbackend.model.dto.comment.CommentAddRequest;
import com.xhs.clothingpatternbackend.model.dto.comment.CommentQueryRequest;
import com.xhs.clothingpatternbackend.model.entity.Comment;
import com.xhs.clothingpatternbackend.model.entity.User;
import com.xhs.clothingpatternbackend.model.vo.AdminCommentVO;
import com.xhs.clothingpatternbackend.model.vo.CommentStatisticsVO;
import com.xhs.clothingpatternbackend.model.vo.CommentVO;
import com.xhs.clothingpatternbackend.model.vo.PageResult;
import com.xhs.clothingpatternbackend.service.CommentService;
import com.xhs.clothingpatternbackend.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 评论接口
 */
@RestController
@RequestMapping("/comment")
@Slf4j
public class CommentController {

    @Autowired
    private CommentService commentService;

    @Autowired
    private UserService userService;

    /**
     * 添加评论
     */
    @PostMapping("/add")
    public BaseResponse<CommentVO> addComment(@Valid @RequestBody CommentAddRequest request,
            HttpServletRequest httpRequest) {
        User loginUser = userService.getLoginUser(httpRequest);
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NOT_LOGIN_ERROR);

        CommentVO comment = commentService.addComment(request, loginUser.getId());
        return ResultUtils.success(comment);
    }

    /**
     * 查询图案的评论列表
     */
    @PostMapping("/list")
    public BaseResponse<PageResult<CommentVO>> getPatternComments(
            @Valid @RequestBody CommentQueryRequest request,
            HttpServletRequest httpRequest) {

        // 获取当前用户ID（未登录用户为null）
        Long currentUserId = null;
        try {
            User loginUser = userService.getLoginUser(httpRequest);
            if (loginUser != null) {
                currentUserId = loginUser.getId();
            }
        } catch (Exception e) {
            // 未登录，保持currentUserId为null
        }

        PageResult<CommentVO> result = commentService.getCommentList(request, currentUserId);
        return ResultUtils.success(result);
    }

    /**
     * 获取评论详情
     */
    @GetMapping("/get/{commentId}")
    public BaseResponse<CommentVO> getCommentDetail(@PathVariable Long commentId,
            HttpServletRequest httpRequest) {
        ThrowUtils.throwIf(commentId == null || commentId <= 0, ErrorCode.PARAMS_ERROR);

        // 获取当前用户ID（未登录用户为null）
        Long currentUserId = null;
        try {
            User loginUser = userService.getLoginUser(httpRequest);
            if (loginUser != null) {
                currentUserId = loginUser.getId();
            }
        } catch (Exception e) {
            // 未登录，保持currentUserId为null
        }

        CommentVO comment = commentService.getCommentDetail(commentId, currentUserId);
        return ResultUtils.success(comment);
    }

    /**
     * 删除评论
     */
    @PostMapping("/delete/{commentId}")
    public BaseResponse<Boolean> deleteCommentByUser(@PathVariable Long commentId,
            HttpServletRequest httpRequest) {
        User loginUser = userService.getLoginUser(httpRequest);
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NOT_LOGIN_ERROR);
        ThrowUtils.throwIf(commentId == null || commentId <= 0, ErrorCode.PARAMS_ERROR);

        boolean result = commentService.deleteComment(commentId, loginUser.getId());
        return ResultUtils.success(result);
    }

    /**
     * 查询图案评论统计信息
     */
    @GetMapping("/statistics/{patternId}")
    public BaseResponse<CommentStatisticsVO> getCommentStatistics(@PathVariable Long patternId) {
        ThrowUtils.throwIf(patternId == null || patternId <= 0, ErrorCode.PARAMS_ERROR);

        CommentStatisticsVO statistics = commentService.getCommentStatistics(patternId);
        return ResultUtils.success(statistics);
    }

    /**
     * 点赞/取消点赞评论
     */
    @PostMapping("/like/{commentId}")
    public BaseResponse<Boolean> toggleCommentLike(@PathVariable Long commentId,
            HttpServletRequest httpRequest) {
        User loginUser = userService.getLoginUser(httpRequest);
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NOT_LOGIN_ERROR);
        ThrowUtils.throwIf(commentId == null || commentId <= 0, ErrorCode.PARAMS_ERROR);

        boolean liked = commentService.toggleCommentLike(commentId, loginUser.getId());
        return ResultUtils.success(liked);
    }

    /**
     * 获取评论的所有回复
     */
    @GetMapping("/replies/{commentId}")
    public BaseResponse<java.util.List<CommentVO>> getCommentReplies(@PathVariable Long commentId,
            HttpServletRequest httpRequest) {
        ThrowUtils.throwIf(commentId == null || commentId <= 0, ErrorCode.PARAMS_ERROR);

        // 获取当前用户ID（未登录用户为null）
        Long currentUserId = null;
        try {
            User loginUser = userService.getLoginUser(httpRequest);
            if (loginUser != null) {
                currentUserId = loginUser.getId();
            }
        } catch (Exception e) {
            // 未登录，保持currentUserId为null
        }

        java.util.List<CommentVO> replies = commentService.getCommentReplies(commentId, currentUserId);
        return ResultUtils.success(replies);
    }

    /**
     * 获取所有的评论（仅管理员）
     */
    @PostMapping("/list/page/vo")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Page<AdminCommentVO>> listAdminCommentVOByPage(
            @RequestBody CommentQueryRequest commentQueryRequest) {
        ThrowUtils.throwIf(commentQueryRequest == null, ErrorCode.PARAMS_ERROR);
        long current = commentQueryRequest.getCurrent();
        long size = commentQueryRequest.getPageSize();
        Page<Comment> commentPage = commentService.page(new Page<>(current, size),
                commentService.getQueryWrapper(commentQueryRequest));
        Page<AdminCommentVO> adminCommentVOPage = new Page<>(current, size, commentPage.getTotal());
        List<AdminCommentVO> adminCommentVOList = commentService.getCommentVOList(commentPage.getRecords());
        adminCommentVOPage.setRecords(adminCommentVOList);
        return ResultUtils.success(adminCommentVOPage);
    }

    /**
     * 删除评论（仅管理员）
     */
    @PostMapping("/admin/delete")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> deleteComment(@RequestBody DeleteRequest deleteRequest) {
        ThrowUtils.throwIf(deleteRequest == null || deleteRequest.getId() <= 0, ErrorCode.PARAMS_ERROR);
        boolean result = commentService.removeById(deleteRequest.getId());
        return ResultUtils.success(result);
    }
}
