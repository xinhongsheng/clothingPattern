package com.xhs.clothingpatternbackend.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xhs.clothingpatternbackend.exception.BusinessException;
import com.xhs.clothingpatternbackend.exception.ErrorCode;
import com.xhs.clothingpatternbackend.mapper.PatternMapper;
import com.xhs.clothingpatternbackend.model.entity.Pattern;
import com.xhs.clothingpatternbackend.model.entity.UserLike;
import com.xhs.clothingpatternbackend.model.vo.LikeOperation;
import com.xhs.clothingpatternbackend.model.vo.LikeResultVO;
import com.xhs.clothingpatternbackend.service.LikeService;
import com.xhs.clothingpatternbackend.mapper.LikeMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

import static com.xhs.clothingpatternbackend.constant.LikeConstant.LIKE_COUNT_KEY_PREFIX;
import static com.xhs.clothingpatternbackend.constant.LikeConstant.LIKE_KEY_PREFIX;

/**
* @author 小辛
* @description 针对表【like(图案点赞表)】的数据库操作Service实现
* @createDate 2025-11-25 13:04:21
*/
@Service
@Slf4j
public class LikeServiceImpl extends ServiceImpl<LikeMapper, UserLike>
    implements LikeService{

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private PatternMapper patternMapper;

    @Autowired
    private  LikeMapper likeMapper;


    /**
     * 处理点赞/取消点赞
     */
    @Transactional
    @Override
    public LikeResultVO handleLike(Long userId, Long patternId) {
        // 1. 检查图案是否存在且已审核通过
        Pattern pattern = patternMapper.selectById(patternId);
        if (pattern == null || !"APPROVED".equals(pattern.getAuditStatus())) {
            throw new BusinessException( ErrorCode.NO_AUTH_ERROR, "图案不存在或未审核通过");
        }

        String likeKey = LIKE_KEY_PREFIX + patternId;
        String countKey = LIKE_COUNT_KEY_PREFIX + patternId;

        // 2. 检查用户当前点赞状态（优先Redis，未命中则查数据库）
        Boolean hasLiked = (Boolean) redisTemplate.opsForHash().get(likeKey, userId.toString());
        boolean currentStatus;
        
        if (hasLiked != null) {
            // Redis命中
            currentStatus = hasLiked;
        } else {
            // Redis未命中，查询数据库
            Long count = likeMapper.countValidLike(userId, patternId);
            currentStatus = count != null && count > 0;
            // 不在这里写入Redis，后面统一写入新状态
        }

        // 3. 切换状态：点赞 -> 取消，取消 -> 点赞
        boolean newStatus = !currentStatus;
        int likeChange = newStatus ? 1 : -1;

        // 4. 更新Redis
        redisTemplate.opsForHash().put(likeKey, userId.toString(), newStatus);

        // 5. 更新点赞计数
        Long currentCount = redisTemplate.opsForValue().increment(countKey, likeChange);
        if (currentCount == null || currentCount < 0) {
            // 如果Redis中没有计数，先从数据库获取
            Integer dbCount = patternMapper.selectLikeCount(patternId);
            currentCount = dbCount != null ? dbCount.longValue() : 0L;
            currentCount = Math.max(0, currentCount + likeChange);
            redisTemplate.opsForValue().set(countKey, currentCount);
        }

        // 6. 记录操作日志（可选，用于数据恢复）
//        redisTemplate.opsForList().leftPush("like:operation:log",
//                new LikeOperation(userId, patternId, newStatus, LocalDateTime.now()));

        return new LikeResultVO(newStatus, currentCount);
    }

    /**
     * 获取用户对图案的点赞状态
     */
    @Override
    public boolean getLikeStatus(Long userId, Long patternId) {
        String likeKey = LIKE_KEY_PREFIX + patternId;
        Boolean status = (Boolean) redisTemplate.opsForHash().get(likeKey, userId.toString());
        
        // 如果Redis中有数据，直接返回
        if (status != null) {
            return status;
        }
        
        // Redis未命中，查询数据库
        Long count = likeMapper.countValidLike(userId, patternId);
        boolean isLiked = count != null && count > 0;
        
        // 写入Redis缓存
        redisTemplate.opsForHash().put(likeKey, userId.toString(), isLiked);
        
        return isLiked;
    }

    /**
     * 获取图案点赞数量
     */
    @Override
    public long getLikeCount(Long patternId) {
        String countKey = LIKE_COUNT_KEY_PREFIX + patternId;
        Object count = redisTemplate.opsForValue().get(countKey);
        
        // Redis命中，直接返回
        if (count != null) {
            if (count instanceof Long) {
                return (Long) count;
            }
            if (count instanceof Integer) {
                return ((Integer) count).longValue();
            }
            if (count instanceof String) {
                return Long.parseLong((String) count);
            }
        }
        
        // Redis未命中，查询数据库
        Integer dbCount = patternMapper.selectLikeCount(patternId);
        long likeCount = dbCount != null ? dbCount.longValue() : 0L;
        
        // 写入Redis缓存
        redisTemplate.opsForValue().set(countKey, likeCount);
        
        return likeCount;
    }

    /**
     * 批量获取用户点赞状态
     */
    @Override
    public Map<Long, Boolean> getBatchLikeStatus(Long userId, List<Long> patternIds) {
        Map<Long, Boolean> result = new HashMap<>();
        List<Long> missedPatternIds = new ArrayList<>();

        // 先从Redis获取
        for (Long patternId : patternIds) {
            String likeKey = LIKE_KEY_PREFIX + patternId;
            Boolean status = (Boolean) redisTemplate.opsForHash().get(likeKey, userId.toString());
            if (status != null) {
                result.put(patternId, status);
            } else {
                missedPatternIds.add(patternId);
            }
        }

        // Redis未命中的，批量查询数据库
        if (!missedPatternIds.isEmpty()) {
            List<Long> likedPatternIds = likeMapper.selectLikedPatternIds(userId, missedPatternIds);
            Set<Long> likedSet = new HashSet<>(likedPatternIds);
            
            for (Long patternId : missedPatternIds) {
                boolean isLiked = likedSet.contains(patternId);
                result.put(patternId, isLiked);
                
                // 写入Redis缓存
                String likeKey = LIKE_KEY_PREFIX + patternId;
                redisTemplate.opsForHash().put(likeKey, userId.toString(), isLiked);
            }
        }

        return result;
    }

    /**
     * 预热点赞数据到Redis
     */
    @Override
    public void warmUpLikeData(Long patternId) {
        try {
            log.debug("预热点赞数据，patternId: {}", patternId);

            String likeKey = LIKE_KEY_PREFIX + patternId;
            String countKey = LIKE_COUNT_KEY_PREFIX + patternId;

            // 1. 预热点赞总数（存储为Long类型，支持increment操作）
            Integer mysqlLikeCount = patternMapper.selectLikeCount(patternId);
            if (mysqlLikeCount != null) {
                redisTemplate.opsForValue().set(countKey, mysqlLikeCount.longValue());
                log.debug("预热图案 {} 点赞数: {}", patternId, mysqlLikeCount);
            } else {
                // 如果数据库中没有记录，初始化为0
                redisTemplate.opsForValue().set(countKey, 0L);
            }

            // 2. 预热用户点赞状态
            List<Long> likedUserIds = likeMapper.selectLikedUserIds(patternId);
            if (!likedUserIds.isEmpty()) {
                Map<String, Boolean> userLikeMap = new HashMap<>();
                for (Long userId : likedUserIds) {
                    userLikeMap.put(userId.toString(), true);
                }
                redisTemplate.opsForHash().putAll(likeKey, userLikeMap);
                log.debug("预热图案 {} 用户点赞状态，涉及用户数: {}", patternId, likedUserIds.size());
            }

            // 3. 设置过期时间（可选，根据业务需求）
            // redisTemplate.expire(likeKey, Duration.ofDays(7));
            // redisTemplate.expire(countKey, Duration.ofDays(7));

        } catch (Exception e) {
            log.error("预热点赞数据失败, patternId: {}", patternId, e);
        }
    }

    /**
     * 批量预热点赞数据
     */
    @Override
    public void batchWarmUpLikeData(List<Long> patternIds) {
        if (patternIds == null || patternIds.isEmpty()) {
            return;
        }

        log.info("批量预热点赞数据，图案数量: {}", patternIds.size());

        for (Long patternId : patternIds) {
            warmUpLikeData(patternId);
        }
    }

    /**
     * 预热用户相关的点赞数据
     */
    @Override
    public void warmUpUserLikeData(Long userId, List<Long> patternIds) {
        if (patternIds == null || patternIds.isEmpty()) {
            return;
        }

        try {
            // 批量查询用户对这些图案的点赞状态
            List<Long> likedPatternIds = likeMapper.selectLikedPatternIds(userId, patternIds);
            Set<Long> likedSet = new HashSet<>(likedPatternIds);

            // 预热到Redis
            for (Long patternId : patternIds) {
                String likeKey = LIKE_KEY_PREFIX + patternId;
                boolean isLiked = likedSet.contains(patternId);
                redisTemplate.opsForHash().put(likeKey, userId.toString(), isLiked);
            }

            log.debug("预热用户 {} 的点赞状态，涉及图案数: {}", userId, patternIds.size());

        } catch (Exception e) {
            log.error("预热用户点赞数据失败, userId: {}", userId, e);
        }
    }
}




