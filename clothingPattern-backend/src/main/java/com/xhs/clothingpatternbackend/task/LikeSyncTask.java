package com.xhs.clothingpatternbackend.task;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xhs.clothingpatternbackend.mapper.ArticleMapper;
import com.xhs.clothingpatternbackend.mapper.LikeMapper;
import com.xhs.clothingpatternbackend.mapper.PatternMapper;
import com.xhs.clothingpatternbackend.model.entity.Article;
import com.xhs.clothingpatternbackend.model.entity.Pattern;
import com.xhs.clothingpatternbackend.model.entity.UserLike;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
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
 * @Description: 定时任务 将Redis中的点赞数据和浏览量同步到数据库中
 * @Version: 1.0
 */
@Component
@Slf4j
public class LikeSyncTask {
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private PatternMapper patternMapper;

    @Autowired
    private LikeMapper likeMapper;

    @Autowired
    private ArticleMapper articleMapper;

    private static final String ARTICLE_VIEW_KEY_PREFIX = "article:view:";
    private static final String ARTICLE_LIKE_KEY_PREFIX = "article:like:";
    private static final String ARTICLE_LIKE_COUNT_KEY_PREFIX = "article:like:count:";

    /**
     * 每5分钟同步一次到MySQL
     */
//    @Scheduled(fixedRate = 1 * 60 * 1000) // 测试1分钟
    @Scheduled(fixedRate = 5 * 60 * 1000) // 5分钟
    @Transactional
    public void syncLikeDataToMySQL() {
        log.info("开始同步点赞数据到MySQL...");

        // 1. 同步图案点赞计数
        syncLikeCounts();

        // 2. 同步图案用户点赞关系
        syncUserLikeRelations();

        // 3. 同步文章浏览量
        syncArticleViewCounts();

        // 4. 同步文章点赞数据
        syncArticleLikeCounts();

        log.info("点赞数据同步完成");
    }

    /**
     * 同步文章浏览量到数据库
     */
    private void syncArticleViewCounts() {
        try {
            Set<String> viewKeys = stringRedisTemplate.keys(ARTICLE_VIEW_KEY_PREFIX + "*");
            if (viewKeys == null || viewKeys.isEmpty()) {
                log.debug("没有需要同步的文章浏览量数据");
                return;
            }

            int syncCount = 0;
            int totalViews = 0;
            for (String viewKey : viewKeys) {
                try {
                    // 从key中提取文章ID
                    String articleIdStr = viewKey.substring(ARTICLE_VIEW_KEY_PREFIX.length());
                    Long articleId = Long.parseLong(articleIdStr);

                    // 获取Redis中的浏览量增量
                    String viewCountStr = stringRedisTemplate.opsForValue().get(viewKey);
                    if (viewCountStr != null && !viewCountStr.isEmpty()) {
                        int incrementCount = Integer.parseInt(viewCountStr);
                        
                        if (incrementCount > 0) {
                            // 批量增加浏览量（一次性增加Redis中累计的次数）
                            articleMapper.incrementViewCountBatch(articleId, incrementCount);
                            
                            // 同步成功后删除Redis中的key
                            stringRedisTemplate.delete(viewKey);
                            
                            syncCount++;
                            totalViews += incrementCount;
                            log.debug("同步文章 {} 浏览量: +{}", articleId, incrementCount);
                        }
                    }
                } catch (NumberFormatException e) {
                    log.error("解析文章浏览量失败, key: {}", viewKey, e);
                } catch (Exception e) {
                    log.error("同步文章浏览量失败, key: {}", viewKey, e);
                }
            }

            if (syncCount > 0) {
                log.info("成功同步 {} 篇文章的浏览量，共 {} 次浏览", syncCount, totalViews);
            }
        } catch (Exception e) {
            log.error("同步文章浏览量时发生异常", e);
        }
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
        try {
            // 查询现有记录（包括已删除的）
            UserLike userLike = likeMapper.selectOneIncludeDeleted(userId, patternId);

            if (liked) {
                // 点赞状态
                if (userLike == null) {
                    // 新增点赞记录 - 使用 INSERT IGNORE 原子操作，避免并发冲突
                    int affectedRows = likeMapper.insertIgnore(
                        com.baomidou.mybatisplus.core.toolkit.IdWorker.getId(),
                        userId,
                        patternId,
                        0 // isDelete = 0 表示有效
                    );
                    if (affectedRows > 0) {
                        log.debug("定时任务：新增点赞记录成功 userId={}, patternId={}", userId, patternId);
                    } else {
                        log.debug("定时任务：点赞记录已存在（并发冲突）userId={}, patternId={}", userId, patternId);
                    }
                } else if (userLike.getIsDelete() == 1) {
                    // 恢复删除的记录
                    likeMapper.updateLikeStatus(userLike.getId(), 0);
                    log.debug("定时任务：恢复点赞记录 userId={}, patternId={}", userId, patternId);
                } else {
                    log.debug("定时任务：点赞记录已存在且有效 userId={}, patternId={}", userId, patternId);
                }
            } else {
                // 取消点赞状态
                if (userLike != null && userLike.getIsDelete() == 0) {
                    // 软删除记录
                    likeMapper.updateLikeStatus(userLike.getId(), 1);
                    log.debug("定时任务：取消点赞记录 userId={}, patternId={}", userId, patternId);
                }
            }
        } catch (Exception e) {
            log.error("定时任务：同步单个用户点赞关系失败 userId={}, patternId={}", userId, patternId, e);
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
     * 清空特定图案的Redis缓存
     */
    public void clearPatternCache(Long patternId) {
        log.info("清空图案 {} 的Redis缓存", patternId);
        String likeKey = LIKE_KEY_PREFIX + patternId;
        String countKey = LIKE_COUNT_KEY_PREFIX + patternId;
        redisTemplate.delete(likeKey);
        redisTemplate.delete(countKey);
        log.info("已清空图案 {} 的Redis缓存", patternId);
    }

    /**
     * 清空所有点赞相关的Redis缓存
     */
    public void clearAllCache() {
        log.info("开始清空所有点赞Redis缓存");
        Set<String> likeKeys = redisTemplate.keys(LIKE_KEY_PREFIX + "*");
        Set<String> countKeys = redisTemplate.keys(LIKE_COUNT_KEY_PREFIX + "*");
        
        if (likeKeys != null && !likeKeys.isEmpty()) {
            redisTemplate.delete(likeKeys);
            log.info("已删除 {} 个点赞状态缓存", likeKeys.size());
        }
        
        if (countKeys != null && !countKeys.isEmpty()) {
            redisTemplate.delete(countKeys);
            log.info("已删除 {} 个点赞计数缓存", countKeys.size());
        }
        
        log.info("所有点赞Redis缓存清空完成");
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

    /**
     * 同步文章点赞数据到数据库
     */
    private void syncArticleLikeCounts() {
        try {
            Set<String> countKeys = stringRedisTemplate.keys(ARTICLE_LIKE_COUNT_KEY_PREFIX + "*");
            if (countKeys == null || countKeys.isEmpty()) {
                log.debug("没有需要同步的文章点赞数据");
                return;
            }

            int syncCount = 0;
            for (String countKey : countKeys) {
                try {
                    // 从key中提取文章ID
                    String articleIdStr = countKey.substring(ARTICLE_LIKE_COUNT_KEY_PREFIX.length());
                    Long articleId = Long.parseLong(articleIdStr);

                    // 获取Redis中的点赞数
                    String countStr = stringRedisTemplate.opsForValue().get(countKey);
                    if (countStr != null && !countStr.isEmpty()) {
                        int likeCount = Integer.parseInt(countStr);
                        
                        // 直接设置数据库中的点赞数（覆盖式更新）
                        // 因为Redis中的值是最新的准确值
                        articleMapper.updateById(new com.xhs.clothingpatternbackend.model.entity.Article() {{
                            setId(articleId);
                            setLikeCount(likeCount);
                        }});
                        
                        syncCount++;
                        log.debug("同步文章 {} 点赞数: {}", articleId, likeCount);
                    }
                } catch (NumberFormatException e) {
                    log.error("解析文章点赞数失败, key: {}", countKey, e);
                } catch (Exception e) {
                    log.error("同步文章点赞数失败, key: {}", countKey, e);
                }
            }

            if (syncCount > 0) {
                log.info("成功同步 {} 篇文章的点赞数", syncCount);
            }
        } catch (Exception e) {
            log.error("同步文章点赞数时发生异常", e);
        }
    }
}
