package com.xhs.clothingpatternbackend.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xhs.clothingpatternbackend.mapper.PatternMapper;
import com.xhs.clothingpatternbackend.mapper.PatternSimilarityMapper;
import com.xhs.clothingpatternbackend.mapper.UserBehaviorMapper;
import com.xhs.clothingpatternbackend.model.entity.Pattern;
import com.xhs.clothingpatternbackend.model.entity.PatternSimilarity;
import com.xhs.clothingpatternbackend.model.entity.UserBehavior;
import com.xhs.clothingpatternbackend.model.enums.AuditStatusEnum;
import com.xhs.clothingpatternbackend.model.vo.PatternVO;
import com.xhs.clothingpatternbackend.service.PatternService;
import com.xhs.clothingpatternbackend.service.PatternSimilarityService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.*;
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
    private static final long HOT_PATTERNS_EXPIRE_HOURS = 2;

    @Override
    @SuppressWarnings("unchecked")
    public List<PatternVO> getRecommendations(Long userId, int limit) {
        if (userId == null || userId <= 0) {
            log.debug("用户ID无效，返回热门图案");
            return getHotPatterns(limit);
        }

        // 1. 尝试从 Redis 获取协同过滤预计算的推荐 ID 列表
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

        if (CollUtil.isNotEmpty(patternIds)) {
            List<Long> finalPatternIds = patternIds.stream().limit(limit).collect(Collectors.toList());
            List<Pattern> patterns = patternMapper.selectBatchIds(finalPatternIds);
            if (CollUtil.isNotEmpty(patterns)) {
                return patternService.getPatternVOList(patterns, userId);
            }
        }

        // 2. Redis 无预计算数据，基于用户行为做实时内容推荐
        return getContentBasedRecommendations(userId, limit);
    }

    /**
     * 基于用户行为的实时内容推荐
     * 分析用户浏览/点赞过的图案偏好（风格、季节），推荐相似图案
     */
    private List<PatternVO> getContentBasedRecommendations(Long userId, int limit) {
        // 1. 获取用户交互过的图案ID
        List<Long> interactedPatternIds = userBehaviorMapper.selectPatternIdsByUserId(userId);
        if (CollUtil.isEmpty(interactedPatternIds)) {
            log.debug("用户 {} 无行为记录，返回热门图案", userId);
            return getHotPatterns(limit);
        }

        // 2. 查询用户交互过的图案，提取偏好特征
        List<Pattern> interactedPatterns = patternMapper.selectBatchIds(interactedPatternIds);
        Set<String> preferredStyles = new HashSet<>();
        Set<String> preferredSeasons = new HashSet<>();
        Set<String> preferredAudiences = new HashSet<>();
        for (Pattern p : interactedPatterns) {
            if (p.getStyle() != null && !p.getStyle().isEmpty()) preferredStyles.add(p.getStyle());
            if (p.getSeason() != null && !p.getSeason().isEmpty()) preferredSeasons.add(p.getSeason());
            if (p.getTargetAudience() != null && !p.getTargetAudience().isEmpty()) preferredAudiences.add(p.getTargetAudience());
        }

        // 3. 查询符合偏好但未看过的图案
        QueryWrapper<Pattern> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("auditStatus", AuditStatusEnum.APPROVED.getValue())
                .eq("isDelete", 0)
                .notIn("id", interactedPatternIds);

        // 至少匹配一个偏好维度（风格 > 季节 > 人群）
        boolean hasPreference = false;
        if (!preferredStyles.isEmpty()) {
            queryWrapper.in("style", preferredStyles);
            hasPreference = true;
        } else if (!preferredSeasons.isEmpty()) {
            queryWrapper.in("season", preferredSeasons);
            hasPreference = true;
        } else if (!preferredAudiences.isEmpty()) {
            queryWrapper.in("targetAudience", preferredAudiences);
            hasPreference = true;
        }

        if (hasPreference) {
            queryWrapper.orderByDesc("likeCount").last("LIMIT " + limit);
            List<Pattern> recommended = patternMapper.selectList(queryWrapper);
            if (CollUtil.isNotEmpty(recommended)) {
                log.debug("基于内容推荐为用户 {} 推荐 {} 个图案（偏好: style={}, season={}）",
                        userId, recommended.size(), preferredStyles, preferredSeasons);
                return patternService.getPatternVOList(recommended, userId);
            }
        }

        // 4. 偏好匹配无结果，返回热门图案（排除已看过的）
        return getHotPatternsExcluding(limit, new HashSet<>(interactedPatternIds));
    }

    /**
     * 获取热门图案ID列表（带缓存）
     */
    @SuppressWarnings("unchecked")
    private List<Long> getHotPatternIds(int limit) {
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

        QueryWrapper<Pattern> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("auditStatus", AuditStatusEnum.APPROVED.getValue());
        queryWrapper.eq("isDelete", 0);
        queryWrapper.orderByDesc("likeCount");
        queryWrapper.last("LIMIT " + Math.max(limit, 20));

        List<Pattern> patterns = patternMapper.selectList(queryWrapper);
        List<Long> hotPatternIds = patterns.stream()
                .map(Pattern::getId)
                .collect(Collectors.toList());

        if (!hotPatternIds.isEmpty()) {
            try {
                redisTemplate.opsForValue().set(REDIS_HOT_PATTERNS_KEY, hotPatternIds,
                        HOT_PATTERNS_EXPIRE_HOURS, TimeUnit.HOURS);
            } catch (Exception e) {
                log.warn("缓存热门图案列表到Redis失败: {}", e.getMessage());
            }
        }

        return hotPatternIds.stream().limit(limit).collect(Collectors.toList());
    }

    @Override
    public List<PatternVO> getHotPatterns(int limit) {
        List<Long> hotPatternIds = getHotPatternIds(limit);
        if (CollUtil.isEmpty(hotPatternIds)) {
            return new ArrayList<>();
        }
        List<Pattern> patterns = patternMapper.selectBatchIds(hotPatternIds);
        if (CollUtil.isEmpty(patterns)) {
            return new ArrayList<>();
        }
        return patternService.getPatternVOList(patterns, null);
    }

    /**
     * 获取热门图案（排除已浏览过的）
     */
    private List<PatternVO> getHotPatternsExcluding(int limit, Set<Long> excludeIds) {
        QueryWrapper<Pattern> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("auditStatus", AuditStatusEnum.APPROVED.getValue());
        queryWrapper.eq("isDelete", 0);
        if (!excludeIds.isEmpty()) {
            queryWrapper.notIn("id", excludeIds);
        }
        queryWrapper.orderByDesc("likeCount");
        queryWrapper.last("LIMIT " + limit);

        List<Pattern> patterns = patternMapper.selectList(queryWrapper);
        if (CollUtil.isEmpty(patterns)) {
            return new ArrayList<>();
        }
        return patternService.getPatternVOList(patterns, null);
    }
}
