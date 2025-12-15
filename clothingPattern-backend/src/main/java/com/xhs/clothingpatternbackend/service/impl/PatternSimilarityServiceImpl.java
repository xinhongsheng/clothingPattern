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
import java.util.concurrent.TimeUnit;
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
    private static final String REDIS_HOT_PATTERNS_KEY = "rec:hot:patterns";
    private static final long HOT_PATTERNS_EXPIRE_HOURS = 2; // 热门图案缓存2小时

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

    /**
     * 获取热门图案ID列表（带缓存）
     */
    @SuppressWarnings("unchecked")
    private List<Long> getHotPatternIds(int limit) {
        // 1. 尝试从 Redis 获取热门图案 ID 列表
        try {
            Object cached = redisTemplate.opsForValue().get(REDIS_HOT_PATTERNS_KEY);
            if (cached instanceof List) {
                List<Long> cachedIds = (List<Long>) cached;
                if (!cachedIds.isEmpty()) {
                    log.debug("从Redis获取热门图案ID列表，共{}个", cachedIds.size());
                    return cachedIds.stream().limit(limit).collect(Collectors.toList());
                }
            }
        } catch (Exception e) {
            log.warn("从Redis获取热门图案列表失败: {}", e.getMessage());
        }

        // 2. Redis 未命中，从数据库查询
        QueryWrapper<Pattern> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("auditStatus", AuditStatusEnum.APPROVED.getValue());
        queryWrapper.eq("isDelete", 0);
        queryWrapper.orderByDesc("likeCount");
        queryWrapper.last("LIMIT " + Math.max(limit, 20)); // 多查一些用于缓存

        List<Pattern> patterns = patternMapper.selectList(queryWrapper);
        List<Long> hotPatternIds = patterns.stream()
                .map(Pattern::getId)
                .collect(Collectors.toList());

        // 3. 存入 Redis 缓存
        if (!hotPatternIds.isEmpty()) {
            try {
                redisTemplate.opsForValue().set(REDIS_HOT_PATTERNS_KEY, hotPatternIds, 
                        HOT_PATTERNS_EXPIRE_HOURS, TimeUnit.HOURS);
                log.debug("热门图案ID列表已缓存到Redis，共{}个", hotPatternIds.size());
            } catch (Exception e) {
                log.warn("缓存热门图案列表到Redis失败: {}", e.getMessage());
            }
        }

        return hotPatternIds.stream().limit(limit).collect(Collectors.toList());
    }

    @Override
    public List<PatternVO> getHotPatterns(int limit) {
        // 1. 获取热门图案 ID 列表（带缓存）
        List<Long> hotPatternIds = getHotPatternIds(limit);
        
        if (CollUtil.isEmpty(hotPatternIds)) {
            return new ArrayList<>();
        }

        // 2. 根据 ID 查询详细信息
        List<Pattern> patterns = patternMapper.selectBatchIds(hotPatternIds);
        if (CollUtil.isEmpty(patterns)) {
            return new ArrayList<>();
        }

        // 3. 转换为 VO
        return patternService.getPatternVOList(patterns, null);
    }
}
