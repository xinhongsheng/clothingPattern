package com.xhs.clothingpatternbackend.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xhs.clothingpatternbackend.mapper.PatternMapper;
import com.xhs.clothingpatternbackend.mapper.PatternSimilarityMapper;
import com.xhs.clothingpatternbackend.mapper.UserBehaviorMapper;
import com.xhs.clothingpatternbackend.model.entity.Pattern;
import com.xhs.clothingpatternbackend.model.entity.PatternSimilarity;
import com.xhs.clothingpatternbackend.model.enums.AuditStatusEnum;
import com.xhs.clothingpatternbackend.model.vo.PatternVO;
import com.xhs.clothingpatternbackend.service.PatternService;
import com.xhs.clothingpatternbackend.service.PatternSimilarityService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 图案相似度 Service 实现
 * @author xhs
 */
@Slf4j
@Service
public class PatternSimilarityServiceImpl extends ServiceImpl<PatternSimilarityMapper, PatternSimilarity>
        implements PatternSimilarityService {

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    @Resource
    private UserBehaviorMapper userBehaviorMapper;

    @Resource
    private PatternMapper patternMapper;

    @Resource
    private PatternService patternService;

    private static final String REDIS_REC_PREFIX = "rec:user:";

    @Override
    @SuppressWarnings("unchecked")
    public List<PatternVO> getRecommendations(Long userId, int limit) {
        if (userId == null || userId <= 0) {
            log.debug("用户ID无效，返回热门图案");
            return getHotPatterns(limit);
        }

        // 1. 尝试从 Redis 获取推荐 ID 列表
        String key = REDIS_REC_PREFIX + userId;
        List<Long> patternIds = null;
        try {
            Object cached = redisTemplate.opsForValue().get(key);
            if (cached instanceof List) {
                patternIds = (List<Long>) cached;
            }
        } catch (Exception e) {
            log.warn("从Redis获取推荐列表失败: {}", e.getMessage());
        }

        // 2. 如果 Redis 里没数据（冷启动问题），则返回热门图案
        if (CollUtil.isEmpty(patternIds)) {
            log.debug("用户 {} 无推荐数据，返回热门图案", userId);
            return getHotPatterns(limit);
        }

        // 3. 根据 ID 列表去数据库查详细信息
        List<Long> finalPatternIds = patternIds.stream().limit(limit).collect(Collectors.toList());
        List<Pattern> patterns = patternMapper.selectBatchIds(finalPatternIds);

        if (CollUtil.isEmpty(patterns)) {
            return getHotPatterns(limit);
        }

        // 4. 转换为 VO
        return patternService.getPatternVOList(patterns, userId);
    }

    @Override
    public List<PatternVO> getHotPatterns(int limit) {
        // 按点赞数排序获取热门图案
        QueryWrapper<Pattern> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("auditStatus", AuditStatusEnum.APPROVED.getValue());
        queryWrapper.eq("isDelete", 0);
        queryWrapper.orderByDesc("likeCount");
        queryWrapper.last("LIMIT " + limit);

        List<Pattern> patterns = patternMapper.selectList(queryWrapper);
        if (CollUtil.isEmpty(patterns)) {
            return new ArrayList<>();
        }

        return patternService.getPatternVOList(patterns, null);
    }
}
