package com.xhs.clothingpatternbackend.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xhs.clothingpatternbackend.model.entity.PatternSimilarity;
import com.xhs.clothingpatternbackend.model.vo.PatternVO;

import java.util.List;

/**
 * 图案相似度 Service
 * @author xhs
 */
public interface PatternSimilarityService extends IService<PatternSimilarity> {

    /**
     * 获取用户推荐列表
     * @param userId 用户ID
     * @param limit 推荐数量
     * @return 推荐的图案列表
     */
    List<PatternVO> getRecommendations(Long userId, int limit);

    /**
     * 获取热门图案（冷启动降级策略）
     * @param limit 数量
     * @return 热门图案列表
     */
    List<PatternVO> getHotPatterns(int limit);
}
