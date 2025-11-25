package com.xhs.clothingpatternbackend.service;

import com.xhs.clothingpatternbackend.model.entity.UserLike;
import com.baomidou.mybatisplus.extension.service.IService;

/**
* @author 小辛
* @description 针对表【like(图案点赞表)】的数据库操作Service
* @createDate 2025-11-25 13:04:21
*/
public interface LikeService extends IService<UserLike> {

    /**
     * 点赞/取消点赞（切换状态）
     * 
     * @param patternId 图案ID
     * @param userId 用户ID
     * @return true-已点赞，false-已取消点赞
     */
    boolean toggleLike(Long patternId, Long userId);

    /**
     * 获取图案点赞数
     * 
     * @param patternId 图案ID
     * @return 点赞数
     */
    long getLikeCount(Long patternId);

    /**
     * 检查用户是否点赞了某个图案
     * 
     * @param patternId 图案ID
     * @param userId 用户ID
     * @return true-已点赞，false-未点赞
     */
    boolean isLiked(Long patternId, Long userId);
}
