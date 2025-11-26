package com.xhs.clothingpatternbackend.controller;

import com.xhs.clothingpatternbackend.common.BaseResponse;
import com.xhs.clothingpatternbackend.common.ResultUtils;
import com.xhs.clothingpatternbackend.exception.ErrorCode;
import com.xhs.clothingpatternbackend.exception.ThrowUtils;
import com.xhs.clothingpatternbackend.model.entity.User;
import com.xhs.clothingpatternbackend.model.vo.LikeResultVO;
import com.xhs.clothingpatternbackend.service.LikeService;
import com.xhs.clothingpatternbackend.service.UserService;
import com.xhs.clothingpatternbackend.task.LikeSyncTask;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

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

    @Resource
    private LikeSyncTask likeSyncTask;

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
        
        LikeResultVO result = likeService.handleLike(loginUser.getId(), patternId);
        
        return ResultUtils.success(result);
    }

    /**
     * 批量获取用户点赞状态
     *
     * @param patternIds 图案ID列表
     * @param request
     * @return 点赞状态Map，键为图案ID，值为点赞状态（true-已点赞，false-未点赞）
     */
    @GetMapping("/batch-status")
    public BaseResponse<Map<Long, Boolean>> getBatchLikeStatus(@RequestBody List<Long> patternIds,HttpServletRequest request) {
        ThrowUtils.throwIf(patternIds == null || patternIds.isEmpty(), ErrorCode.PARAMS_ERROR, "图案ID不能为空");

        User loginUser = userService.getLoginUser(request);
        Map<Long,Boolean> statusMap= likeService.getBatchLikeStatus(loginUser.getId(), patternIds);
        
        return ResultUtils.success(statusMap);
    }

    /**
     * 检查用户是否点赞了某个图案
     * 
     * @param patternId 图案ID
     * @param request
     * @return true-已点赞，false-未点赞
     */
    @GetMapping("/status")
    public BaseResponse<Boolean> getLikeStatus(@RequestParam Long patternId, HttpServletRequest request) {
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
        
        boolean isLiked = likeService.getLikeStatus( loginUser.getId(),patternId);
        
        return ResultUtils.success(isLiked);
    }

    /**
     * 修复Redis中点赞计数的数据类型（仅管理员可用）
     * 将字符串类型转换为Long类型
     */
    @PostMapping("/fix-data-type")
    public BaseResponse<String> fixLikeCountDataType() {
        likeSyncTask.fixLikeCountDataType();
        return ResultUtils.success("数据类型修复完成");
    }
}
