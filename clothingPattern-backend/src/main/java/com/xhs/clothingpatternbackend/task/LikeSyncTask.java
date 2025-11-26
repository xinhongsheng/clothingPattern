package com.xhs.clothingpatternbackend.task;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xhs.clothingpatternbackend.mapper.LikeMapper;
import com.xhs.clothingpatternbackend.mapper.PatternMapper;
import com.xhs.clothingpatternbackend.model.entity.Pattern;
import com.xhs.clothingpatternbackend.model.entity.UserLike;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.Set;

import static com.xhs.clothingpatternbackend.constant.LikeConstant.LIKE_COUNT_KEY_PREFIX;
import static com.xhs.clothingpatternbackend.constant.LikeConstant.LIKE_KEY_PREFIX;

/**
 * @Author: 小辛同学
 * @CreateTime: 2025-11-26
 * @Description: 定时任务 将Redis中的点赞数据同步到数据库中
 * @Version: 1.0
 */
@Component
@Slf4j
public class LikeSyncTask {
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private PatternMapper patternMapper;

    @Autowired
    private LikeMapper likeMapper;

    /**
     * 每5分钟同步一次到MySQL
     */
    @Scheduled(fixedRate = 1 * 60 * 1000) // 测试1分钟
//    @Scheduled(fixedRate = 5 * 60 * 1000) // 5分钟
    @Transactional
    public void syncLikeDataToMySQL() {
        log.info("开始同步点赞数据到MySQL...");

        // 1. 同步点赞计数
        syncLikeCounts();

        // 2. 同步用户点赞关系
        syncUserLikeRelations();

        log.info("点赞数据同步完成");
    }

    private void syncLikeCounts() {
        Set<String> countKeys = redisTemplate.keys(LIKE_COUNT_KEY_PREFIX + "*");
        if (countKeys == null) return;

        for (String countKey : countKeys) {
            try {
                Long patternId = Long.parseLong(countKey.substring(LIKE_COUNT_KEY_PREFIX.length()));
                Object countObj = redisTemplate.opsForValue().get(countKey);
                if (countObj != null) {
                    int likeCount;
                    // 兼容多种数据类型
                    if (countObj instanceof Long) {
                        likeCount = ((Long) countObj).intValue();
                    } else if (countObj instanceof Integer) {
                        likeCount = (Integer) countObj;
                    } else if (countObj instanceof String) {
                        likeCount = Integer.parseInt((String) countObj);
                    } else {
                        log.warn("未知的点赞数类型: {}, patternId: {}", countObj.getClass(), patternId);
                        continue;
                    }

                    // 更新图案表点赞数
                    Pattern pattern = new Pattern();
                    pattern.setId(patternId);
                    pattern.setLikeCount(likeCount);
                    patternMapper.updateById(pattern);

                    log.debug("同步图案 {} 点赞数: {}", patternId, likeCount);
                }
            } catch (Exception e) {
                log.error("同步点赞计数失败, key: {}", countKey, e);
            }
        }
    }

    private void syncUserLikeRelations() {
        Set<String> likeKeys = redisTemplate.keys(LIKE_KEY_PREFIX + "*");
        if (likeKeys == null) return;

        for (String likeKey : likeKeys) {
            try {
                Long patternId = Long.parseLong(likeKey.substring(LIKE_KEY_PREFIX.length()));
                Map<Object, Object> userLikeMap = redisTemplate.opsForHash().entries(likeKey);

                for (Map.Entry<Object, Object> entry : userLikeMap.entrySet()) {
                    Long userId = Long.parseLong(entry.getKey().toString());
                    boolean liked = Boolean.parseBoolean(entry.getValue().toString());

                    syncSingleUserLike(userId, patternId, liked);
                }

            } catch (Exception e) {
                log.error("同步用户点赞关系失败, key: {}", likeKey, e);
            }
        }
    }

    private void syncSingleUserLike(Long userId, Long patternId, boolean liked) {
        // 查询现有记录
        LambdaQueryWrapper<UserLike> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(UserLike::getUserId, userId)
                .eq(UserLike::getPatternId, patternId);

        UserLike userLike = likeMapper.selectOne(queryWrapper);

        if (liked) {
            // 点赞状态
            if (userLike == null) {
                // 新增点赞记录
                userLike = new UserLike();
                userLike.setUserId(userId);
                userLike.setPatternId(patternId);
                userLike.setIsDelete(0);
                likeMapper.insert(userLike);
            } else if (userLike.getIsDelete() == 1) {
                // 恢复删除的记录
                userLike.setIsDelete(0);
                likeMapper.updateById(userLike);
            }
        } else {
            // 取消点赞状态
            if (userLike != null && userLike.getIsDelete() == 0) {
                // 软删除记录
                userLike.setIsDelete(1);
                likeMapper.updateById(userLike);
            }
        }
    }

    /**
     * 每天凌晨清理过期数据（可选）
     */
    @Scheduled(cron = "0 0 2 * * ?") // 每天凌晨2点
    public void cleanupExpiredLikeData() {
        log.info("开始清理过期点赞数据...");

        // 清理30天前的点赞操作日志
        redisTemplate.opsForList().trim("like:operation:log", 0, 999);

        log.info("过期点赞数据清理完成");
    }

    /**
     * 修复Redis中点赞计数的数据类型
     * 将字符串类型转换为Long类型，以支持increment操作
     */
    public void fixLikeCountDataType() {
        log.info("开始修复Redis点赞计数数据类型...");

        Set<String> countKeys = redisTemplate.keys(LIKE_COUNT_KEY_PREFIX + "*");
        if (countKeys == null) {
            log.info("没有找到需要修复的点赞计数数据");
            return;
        }

        int fixedCount = 0;
        for (String countKey : countKeys) {
            try {
                Object countObj = redisTemplate.opsForValue().get(countKey);
                if (countObj instanceof String) {
                    // 将字符串转换为Long
                    Long count = Long.parseLong((String) countObj);
                    redisTemplate.opsForValue().set(countKey, count);
                    fixedCount++;
                    log.debug("修复点赞计数: {} -> {}", countKey, count);
                }
            } catch (Exception e) {
                log.error("修复点赞计数数据类型失败, key: {}", countKey, e);
            }
        }

        log.info("Redis点赞计数数据类型修复完成，共修复 {} 条记录", fixedCount);
    }
}
