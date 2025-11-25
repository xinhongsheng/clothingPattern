package com.xhs.clothingpatternbackend.controller;

import com.xhs.clothingpatternbackend.common.BaseResponse;
import com.xhs.clothingpatternbackend.common.ResultUtils;
import com.xhs.clothingpatternbackend.exception.ErrorCode;
import com.xhs.clothingpatternbackend.exception.ThrowUtils;
import com.xhs.clothingpatternbackend.model.entity.User;
import com.xhs.clothingpatternbackend.model.vo.LikeResultVO;
import com.xhs.clothingpatternbackend.service.LikeService;
import com.xhs.clothingpatternbackend.service.UserService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 点赞接口
 */
@RestController
@RequestMapping("/like")
@Slf4j
public class LikeController {

    @Resource
    private LikeService likeService;

    @Resource
    private UserService userService;

    /**
     * 点赞/取消点赞
     * 
     * @param patternId 图案ID
     * @param request
     * @return 点赞结果（包含点赞状态和最新点赞数）
     */
    @PostMapping("/toggle")
    public BaseResponse<LikeResultVO> toggleLike(@RequestParam Long patternId, HttpServletRequest request) {
        ThrowUtils.throwIf(patternId == null || patternId <= 0, ErrorCode.PARAMS_ERROR, "图案ID不能为空");
        
        // 必须登录
        User loginUser = userService.getLoginUser(request);
        
        // 执行点赞/取消点赞
        boolean isLiked = likeService.toggleLike(patternId, loginUser.getId());
        
        // 获取最新点赞数
        long likeCount = likeService.getLikeCount(patternId);
        
        // 构建返回结果
        LikeResultVO result = new LikeResultVO();
        result.setIsLiked(isLiked);
        result.setLikeCount(likeCount);
        
        return ResultUtils.success(result);
    }

    /**
     * 获取图案点赞数
     * 
     * @param patternId 图案ID
     * @return 点赞数
     */
    @GetMapping("/count")
    public BaseResponse<Long> getLikeCount(@RequestParam Long patternId) {
        ThrowUtils.throwIf(patternId == null || patternId <= 0, ErrorCode.PARAMS_ERROR, "图案ID不能为空");
        
        long count = likeService.getLikeCount(patternId);
        
        return ResultUtils.success(count);
    }

    /**
     * 检查用户是否点赞了某个图案
     * 
     * @param patternId 图案ID
     * @param request
     * @return true-已点赞，false-未点赞
     */
    @GetMapping("/check")
    public BaseResponse<Boolean> checkLiked(@RequestParam Long patternId, HttpServletRequest request) {
        ThrowUtils.throwIf(patternId == null || patternId <= 0, ErrorCode.PARAMS_ERROR, "图案ID不能为空");
        
        // 未登录用户返回false
        User loginUser;
        try {
            loginUser = userService.getLoginUser(request);
        } catch (Exception e) {
            return ResultUtils.success(false);
        }
        
        if (loginUser == null || loginUser.getId() == null) {
            return ResultUtils.success(false);
        }
        
        boolean isLiked = likeService.isLiked(patternId, loginUser.getId());
        
        return ResultUtils.success(isLiked);
    }
}
