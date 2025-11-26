package com.xhs.clothingpatternbackend.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xhs.clothingpatternbackend.exception.BusinessException;
import com.xhs.clothingpatternbackend.exception.ErrorCode;
import com.xhs.clothingpatternbackend.mapper.PatternMapper;
import com.xhs.clothingpatternbackend.model.entity.Pattern;
import com.xhs.clothingpatternbackend.model.entity.UserLike;
import com.xhs.clothingpatternbackend.model.vo.LikeResultVO;
import com.xhs.clothingpatternbackend.service.LikeService;
import com.xhs.clothingpatternbackend.mapper.LikeMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    @Autowired
    private org.springframework.data.redis.core.StringRedisTemplate stringRedisTemplate;


    /**
     * 处理点赞/取消点赞
     */
    @Transactional
    @Override
    public LikeResultVO handleLike(Long userId, Long patternId) {
        log.info("开始处理点赞操作: userId={}, patternId={}", userId, patternId);
        
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
            log.info("从Redis获取点赞状态: userId={}, patternId={}, currentStatus={}", userId, patternId, currentStatus);
        } else {
            // Redis未命中，查询数据库
            Long count = likeMapper.countValidLike(userId, patternId);
            currentStatus = count != null && count > 0;
            log.info("从数据库获取点赞状态: userId={}, patternId={}, count={}, currentStatus={}", userId, patternId, count, currentStatus);
            // 不在这里写入Redis，后面统一写入新状态
        }

        // 3. 切换状态：点赞 -> 取消，取消 -> 点赞
        boolean newStatus = !currentStatus;
        int likeChange = newStatus ? 1 : -1;
        log.info("切换点赞状态: userId={}, patternId={}, currentStatus={} -> newStatus={}", userId, patternId, currentStatus, newStatus);

        // 4. 更新Redis
        redisTemplate.opsForHash().put(likeKey, userId.toString(), newStatus);
        log.info("更新Redis点赞状态: userId={}, patternId={}, newStatus={}", userId, patternId, newStatus);

        // 5. 更新点赞计数
        Long currentCount = redisTemplate.opsForValue().increment(countKey, likeChange);
        if (currentCount == null || currentCount < 0) {
            // 如果Redis中没有计数，先从数据库获取
            Integer dbCount = patternMapper.selectLikeCount(patternId);
            currentCount = dbCount != null ? dbCount.longValue() : 0L;
            currentCount = Math.max(0, currentCount + likeChange);
            redisTemplate.opsForValue().set(countKey, currentCount);
            log.info("Redis点赞计数为空，从数据库获取并更新: patternId={}, dbCount={}, currentCount={}", patternId, dbCount, currentCount);
        } else {
            log.info("更新Redis点赞计数: patternId={}, currentCount={}", patternId, currentCount);
        }

        // 6. 立即同步到数据库，确保数据持久化
        log.info("开始同步到数据库: userId={}, patternId={}, newStatus={}, currentCount={}", userId, patternId, newStatus, currentCount);
        syncToDatabase(userId, patternId, newStatus, currentCount.intValue());

        // 7. 删除Redis中的点赞状态和点赞数缓存，强制下次查询从数据库读取
        log.info("开始删除Redis中的点赞缓存: patternId={}", patternId);
        redisTemplate.delete(likeKey);
        redisTemplate.delete(countKey);
        log.info("删除Redis缓存完成: likeKey={}, countKey={}", likeKey, countKey);

        // 8. 清空图案列表缓存，确保刷新页面时能获取最新数据
        clearPatternListCache();

        // 9. 记录操作日志（可选，用于数据恢复）
//        redisTemplate.opsForList().leftPush("like:operation:log",
//                new LikeOperation(userId, patternId, newStatus, LocalDateTime.now()));

        log.info("点赞操作完成: userId={}, patternId={}, newStatus={}, currentCount={}", userId, patternId, newStatus, currentCount);
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
            log.info("getLikeStatus从Redis获取: userId={}, patternId={}, status={}", userId, patternId, status);
            return status;
        }
        
        // Redis未命中，查询数据库
        Long count = likeMapper.countValidLike(userId, patternId);
        boolean isLiked = count != null && count > 0;
        log.info("getLikeStatus从数据库获取: userId={}, patternId={}, count={}, isLiked={}", userId, patternId, count, isLiked);
        
        // 写入Redis缓存
        redisTemplate.opsForHash().put(likeKey, userId.toString(), isLiked);
        log.info("getLikeStatus写入Redis缓存: userId={}, patternId={}, isLiked={}", userId, patternId, isLiked);
        
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
            long result = 0L;
            if (count instanceof Long) {
                result = (Long) count;
            } else if (count instanceof Integer) {
                result = ((Integer) count).longValue();
            } else if (count instanceof String) {
                result = Long.parseLong((String) count);
            }
            log.info("getLikeCount从Redis获取: patternId={}, count={}", patternId, result);
            return result;
        }
        
        // Redis未命中，查询数据库
        log.info("getLikeCount Redis未命中，查询数据库: patternId={}", patternId);
        Integer dbCount = patternMapper.selectLikeCount(patternId);
        long likeCount = dbCount != null ? dbCount.longValue() : 0L;
        log.info("getLikeCount从数据库获取: patternId={}, dbCount={}, likeCount={}", patternId, dbCount, likeCount);
        
        // 写入Redis缓存
        redisTemplate.opsForValue().set(countKey, likeCount);
        log.info("getLikeCount写入Redis缓存: patternId={}, likeCount={}", patternId, likeCount);
        
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

    /**
     * 立即同步点赞数据到数据库
     * 确保点赞操作后数据立即持久化，避免刷新页面后状态丢失
     */
    private void syncToDatabase(Long userId, Long patternId, boolean liked, int likeCount) {
        try {
            log.info("syncToDatabase开始: userId={}, patternId={}, liked={}, likeCount={}", userId, patternId, liked, likeCount);
            
            // 1. 同步用户点赞关系
            // 注意：需要使用 selectOneIncludeDeleted 来查询所有记录（包括已删除的），
            // 因为 @TableLogic 会自动过滤 isDelete = 1 的记录
            UserLike userLike = likeMapper.selectOneIncludeDeleted(userId, patternId);
            log.info("查询现有点赞记录: userId={}, patternId={}, userLike={}", userId, patternId, 
                    userLike != null ? "id=" + userLike.getId() + ",isDelete=" + userLike.getIsDelete() : "null");
            
            if (liked) {
                // 点赞状态：需要确保数据库中有有效记录
                if (userLike == null) {
                    // 新增点赞记录
                    userLike = new UserLike();
                    userLike.setUserId(userId);
                    userLike.setPatternId(patternId);
                    userLike.setIsDelete(0);
                    int insertResult = likeMapper.insert(userLike);
                    log.info("新增点赞记录成功: userId={}, patternId={}, id={}, insertResult={}", userId, patternId, userLike.getId(), insertResult);
                } else if (userLike.getIsDelete() == 1) {
                    // 恢复删除的记录 - 使用原生SQL更新，确保isDelete字段被更新
                    int updateResult = likeMapper.updateLikeStatus(userLike.getId(), 0);
                    log.info("恢复点赞记录成功: userId={}, patternId={}, id={}, updateResult={}", userId, patternId, userLike.getId(), updateResult);
                } else {
                    log.info("点赞记录已存在且有效，无需操作: userId={}, patternId={}", userId, patternId);
                }
            } else {
                // 取消点赞状态：软删除记录 - 使用原生SQL更新，确保isDelete字段被更新
                if (userLike != null && userLike.getIsDelete() == 0) {
                    int updateResult = likeMapper.updateLikeStatus(userLike.getId(), 1);
                    log.info("取消点赞记录成功: userId={}, patternId={}, id={}, updateResult={}", userId, patternId, userLike.getId(), updateResult);
                } else {
                    log.info("无需取消点赞，记录不存在或已删除: userId={}, patternId={}", userId, patternId);
                }
            }
            
            // 2. 同步点赞计数到图案表
            Pattern pattern = new Pattern();
            pattern.setId(patternId);
            pattern.setLikeCount(likeCount);
            int updateResult = patternMapper.updateById(pattern);
            log.info("更新图案点赞数: patternId={}, likeCount={}, updateResult={}", patternId, likeCount, updateResult);
            
            log.info("syncToDatabase完成: userId={}, patternId={}, liked={}", userId, patternId, liked);
            
        } catch (Exception e) {
            // 记录错误但不抛出异常，避免影响主流程
            // Redis数据已更新，定时任务会补偿同步
            log.error("同步点赞数据到数据库失败: userId={}, patternId={}, liked={}", 
                    userId, patternId, liked, e);
        }
    }

    /**
     * 清空图案列表缓存
     * 点赞操作后需要清空缓存，确保刷新页面时能获取最新的点赞数据
     */
    private void clearPatternListCache() {
        try {
            log.info("开始清空图案列表缓存...");
            
            // 1. 清空本地 Caffeine 缓存（关键！）
            com.xhs.clothingpatternbackend.controller.PatternController.LOCAL_CACHE.invalidateAll();
            log.info("清空本地Caffeine缓存完成");
            
            // 2. 清空 Redis 中的图案列表缓存
            Set<String> keys = stringRedisTemplate.keys("xhs_pattern:listPictureVOByPage:*");
            log.info("找到 {} 个Redis图案列表缓存", keys != null ? keys.size() : 0);
            
            if (keys != null && !keys.isEmpty()) {
                Long deleteCount = stringRedisTemplate.delete(keys);
                log.info("清空Redis图案列表缓存完成，共清空 {} 个缓存", deleteCount);
            } else {
                log.info("Redis中没有找到需要清空的图案列表缓存");
            }
            
            log.info("图案列表缓存清空完成");
        } catch (Exception e) {
            log.error("清空图案列表缓存失败", e);
        }
    }
}




