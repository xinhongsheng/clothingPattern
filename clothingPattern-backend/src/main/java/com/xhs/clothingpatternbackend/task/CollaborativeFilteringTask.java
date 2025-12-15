package com.xhs.clothingpatternbackend.task;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.xhs.clothingpatternbackend.mapper.PatternSimilarityMapper;
import com.xhs.clothingpatternbackend.mapper.UserBehaviorMapper;
import com.xhs.clothingpatternbackend.model.entity.PatternSimilarity;
import com.xhs.clothingpatternbackend.model.entity.UserBehavior;
import com.xhs.clothingpatternbackend.service.PatternSimilarityService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 协同过滤推荐算法定时任务
 * 基于物品的协同过滤（Item-Based Collaborative Filtering）
 *
 * @author xhs
 */
@Slf4j
@Component
public class CollaborativeFilteringTask {

    @Resource
    private UserBehaviorMapper userBehaviorMapper;

    @Resource
    private PatternSimilarityMapper patternSimilarityMapper;

    @Resource
    private PatternSimilarityService patternSimilarityService;

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    private static final String REDIS_REC_PREFIX = "rec:user:";
    private static final int RECOMMEND_LIMIT = 10;
    private static final double SIMILARITY_THRESHOLD = 0.1;

    /**
     * 每天凌晨 2 点执行协同过滤算法
     * 计算物品相似度矩阵并为每个用户生成推荐列表
     */
    @Scheduled(cron = "0 0 2 * * ?")
    @Transactional(rollbackFor = Exception.class)
    public void calculateSimilarityTask() {
        log.info("========== 开始执行协同过滤推荐算法 ==========");
        long startTime = System.currentTimeMillis();

        try {
            // 1. 获取所有用户行为数据
            List<UserBehavior> behaviorList = userBehaviorMapper.selectList(null);
            if (behaviorList == null || behaviorList.isEmpty()) {
                log.info("没有用户行为数据，跳过推荐算法计算");
                return;
            }
            log.info("获取到 {} 条用户行为数据", behaviorList.size());

            // 2. 数据转换：Map<UserId, List<PatternId>>
            // 这一步是为了知道每个用户喜欢了哪些东西
            Map<Long, List<Long>> userLikesMap = behaviorList.stream()
                    .collect(Collectors.groupingBy(
                            UserBehavior::getUserId,
                            Collectors.mapping(UserBehavior::getPatternId, Collectors.toList())
                    ));
            log.info("共有 {} 个用户有行为记录", userLikesMap.size());

            // 3. 构建同现矩阵 (Co-occurrence Matrix) 和 物品点击量
            // countMap: 记录每个物品被多少用户喜欢过 (分母的一部分)
            Map<Long, Integer> countMap = new HashMap<>();
            // coOccurrenceMap: 记录物品A和物品B同时被多少用户喜欢 (分子)
            Map<Long, Map<Long, Integer>> coOccurrenceMap = new HashMap<>();

            for (List<Long> userLikes : userLikesMap.values()) {
                // 去重，一个用户对同一个物品的多次行为只算一次
                Set<Long> uniqueLikes = new HashSet<>(userLikes);
                List<Long> uniqueList = new ArrayList<>(uniqueLikes);

                for (int i = 0; i < uniqueList.size(); i++) {
                    Long itemA = uniqueList.get(i);
                    countMap.put(itemA, countMap.getOrDefault(itemA, 0) + 1);

                    for (int j = i + 1; j < uniqueList.size(); j++) {
                        Long itemB = uniqueList.get(j);

                        // 填充 A -> B
                        coOccurrenceMap.computeIfAbsent(itemA, k -> new HashMap<>())
                                .put(itemB, coOccurrenceMap.get(itemA).getOrDefault(itemB, 0) + 1);
                        // 填充 B -> A (对称)
                        coOccurrenceMap.computeIfAbsent(itemB, k -> new HashMap<>())
                                .put(itemA, coOccurrenceMap.get(itemB).getOrDefault(itemA, 0) + 1);
                    }
                }
            }
            log.info("构建同现矩阵完成，共有 {} 个物品", countMap.size());

            // 4. 计算相似度并保存到数据库
            calculateAndSaveSimilarity(countMap, coOccurrenceMap);

            // 5. 基于矩阵为每个用户生成推荐列表并存入 Redis
            generateRecommendForUsers(userLikesMap, coOccurrenceMap, countMap);

            long endTime = System.currentTimeMillis();
            log.info("========== 协同过滤推荐算法执行完成，耗时 {} ms ==========", endTime - startTime);

        } catch (Exception e) {
            log.error("协同过滤推荐算法执行失败", e);
            throw e;
        }
    }

    /**
     * 计算相似度并保存到数据库
     */
    private void calculateAndSaveSimilarity(Map<Long, Integer> countMap,
                                             Map<Long, Map<Long, Integer>> coOccurrenceMap) {
        // 清空旧数据
        try {
            patternSimilarityMapper.truncateTable();
            log.info("已清空旧的相似度数据");
        } catch (Exception e) {
            log.warn("清空相似度表失败，使用删除方式: {}", e.getMessage());
            patternSimilarityMapper.delete(null);
        }

        List<PatternSimilarity> saveList = new ArrayList<>();
        Set<String> processedPairs = new HashSet<>(); // 避免重复保存

        for (Map.Entry<Long, Map<Long, Integer>> entryA : coOccurrenceMap.entrySet()) {
            Long itemA = entryA.getKey();
            for (Map.Entry<Long, Integer> entryB : entryA.getValue().entrySet()) {
                Long itemB = entryB.getKey();

                // 避免重复保存 (A-B 和 B-A 只保存一次)
                String pairKey = itemA < itemB ? itemA + "-" + itemB : itemB + "-" + itemA;
                if (processedPairs.contains(pairKey)) {
                    continue;
                }
                processedPairs.add(pairKey);

                Integer intersection = entryB.getValue(); // 分子：同时喜欢A和B的人数

                // 分母：sqrt(喜欢A的人数 * 喜欢B的人数)
                Integer countA = countMap.get(itemA);
                Integer countB = countMap.get(itemB);
                if (countA == null || countB == null || countA == 0 || countB == 0) {
                    continue;
                }

                double denominator = Math.sqrt((double) countA * countB);
                double similarity = intersection / denominator;

                // 只有相似度大于阈值才存，减少垃圾数据
                if (similarity > SIMILARITY_THRESHOLD) {
                    PatternSimilarity ps = new PatternSimilarity();
                    ps.setPatternIdA(itemA);
                    ps.setPatternIdB(itemB);
                    ps.setSimilarity(similarity);
                    saveList.add(ps);
                }
            }
        }

        // 批量保存
        if (!saveList.isEmpty()) {
            patternSimilarityService.saveBatch(saveList, 1000);
            log.info("保存了 {} 条相似度记录", saveList.size());
        } else {
            log.info("没有满足阈值的相似度记录需要保存");
        }
    }

    /**
     * 为每个用户生成推荐列表并存入 Redis
     */
    private void generateRecommendForUsers(Map<Long, List<Long>> userLikesMap,
                                            Map<Long, Map<Long, Integer>> coOccurrenceMap,
                                            Map<Long, Integer> countMap) {
        int userCount = 0;
        for (Long userId : userLikesMap.keySet()) {
            // 获取该用户喜欢过的物品（去重）
            Set<Long> likedItemsSet = new HashSet<>(userLikesMap.get(userId));

            // 候选推荐集：Map<PatternId, Score>
            Map<Long, Double> recommendCandidates = new HashMap<>();

            // 遍历用户喜欢的每一个物品 itemA
            for (Long itemA : likedItemsSet) {
                // 找出与 itemA 相似的物品 itemB
                Map<Long, Integer> similarItems = coOccurrenceMap.get(itemA);
                if (similarItems == null) {
                    continue;
                }

                for (Map.Entry<Long, Integer> entry : similarItems.entrySet()) {
                    Long itemB = entry.getKey();

                    // 如果用户已经看过 itemB，就不推荐了
                    if (likedItemsSet.contains(itemB)) {
                        continue;
                    }

                    // 计算相似度分数
                    Integer intersection = entry.getValue();
                    Integer countA = countMap.get(itemA);
                    Integer countB = countMap.get(itemB);
                    if (countA == null || countB == null || countA == 0 || countB == 0) {
                        continue;
                    }

                    double similarity = intersection / Math.sqrt((double) countA * countB);

                    // 累加推荐分数（多个已喜欢物品都与itemB相似，分数相加）
                    recommendCandidates.put(itemB,
                            recommendCandidates.getOrDefault(itemB, 0.0) + similarity);
                }
            }

            // 对推荐结果排序，取 Top N
            List<Long> topIds = recommendCandidates.entrySet().stream()
                    .sorted((e1, e2) -> Double.compare(e2.getValue(), e1.getValue()))
                    .limit(RECOMMEND_LIMIT)
                    .map(Map.Entry::getKey)
                    .collect(Collectors.toList());

            // 存入 Redis: rec:user:101 -> [5, 8, 12...]
            if (!topIds.isEmpty()) {
                String key = REDIS_REC_PREFIX + userId;
                redisTemplate.opsForValue().set(key, topIds, 24, TimeUnit.HOURS);
                userCount++;
            }
        }

        log.info("已为 {} 个用户生成推荐列表并存入 Redis", userCount);
    }

    /**
     * 手动触发推荐算法（用于测试）
     */
    public void runManually() {
        log.info("手动触发协同过滤推荐算法");
        calculateSimilarityTask();
    }
}
