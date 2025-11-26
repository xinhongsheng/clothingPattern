package com.xhs.clothingpatternbackend.service;

import com.xhs.clothingpatternbackend.model.entity.UserLike;
import com.baomidou.mybatisplus.extension.service.IService;
import com.xhs.clothingpatternbackend.model.vo.LikeResultVO;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

/**
* @author 小辛
* @description 针对表【like(图案点赞表)】的数据库操作Service
* @createDate 2025-11-25 13:04:21
*/
public interface LikeService extends IService<UserLike> {

    /**
     * 处理点赞/取消点赞
     */
    @Transactional
    LikeResultVO handleLike(Long userId, Long patternId);
    /**
     * 获取用户对图案的点赞状态
     */
    boolean getLikeStatus(Long userId, Long patternId);
    /**
     * 获取图案点赞数量
     */
    long getLikeCount(Long patternId);
    /**
     * 批量获取用户点赞状态
     */
    Map<Long, Boolean> getBatchLikeStatus(Long userId, List<Long> patternIds);

    void warmUpLikeData(Long patternId);

    void batchWarmUpLikeData(List<Long> patternIds);

    void warmUpUserLikeData(Long userId, List<Long> patternIds);
}


