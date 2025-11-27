package com.xhs.clothingpatternbackend.service.impl;

import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xhs.clothingpatternbackend.exception.BusinessException;
import com.xhs.clothingpatternbackend.exception.ErrorCode;
import com.xhs.clothingpatternbackend.mapper.ArticleCollectMapper;
import com.xhs.clothingpatternbackend.mapper.ArticleLikeMapper;
import com.xhs.clothingpatternbackend.mapper.ArticleMapper;
import com.xhs.clothingpatternbackend.model.dto.article.ArticleAddRequest;
import com.xhs.clothingpatternbackend.model.dto.article.ArticleQueryRequest;
import com.xhs.clothingpatternbackend.model.entity.Article;
import com.xhs.clothingpatternbackend.model.entity.ArticleCollect;
import com.xhs.clothingpatternbackend.model.entity.ArticleLike;
import com.xhs.clothingpatternbackend.model.vo.ArticleVO;
import com.xhs.clothingpatternbackend.model.vo.CollectResult;
import com.xhs.clothingpatternbackend.model.vo.LikeResult;
import com.xhs.clothingpatternbackend.model.vo.PageResult;
import com.xhs.clothingpatternbackend.service.ArticleService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 文章服务实现
 */
@Service
@Slf4j
public class ArticleServiceImpl extends ServiceImpl<ArticleMapper, Article>
        implements ArticleService {

    @Autowired
    private ArticleMapper articleMapper;

    @Autowired
    private ArticleLikeMapper articleLikeMapper;

    @Autowired
    private ArticleCollectMapper articleCollectMapper;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    private static final String ARTICLE_VIEW_KEY_PREFIX = "article:view:";
    private static final String ARTICLE_LIKE_KEY_PREFIX = "article:like:";
    private static final String ARTICLE_LIKE_COUNT_KEY_PREFIX = "article:like:count:";
    private static final String ARTICLE_COLLECT_KEY_PREFIX = "article:collect:";
    private static final String HOT_ARTICLES_KEY = "article:hot:list";
    private static final String RECOMMEND_ARTICLES_KEY = "article:recommend:list";

    @Override
    public PageResult<ArticleVO> getArticleList(ArticleQueryRequest query, Long currentUserId) {
        // 1. 查询文章列表
        List<ArticleVO> articleList = articleMapper.selectArticleList(query);

        // 2. 批量设置用户交互状态
        if (currentUserId != null && !articleList.isEmpty()) {
            setUserInteractionStatus(articleList, currentUserId);
        }

        // 3. 分页处理（在内存中分页）
        int total = articleList.size();
        int fromIndex = (query.getPageNum() - 1) * query.getPageSize();
        int toIndex = Math.min(fromIndex + query.getPageSize(), total);

        if (fromIndex >= total) {
            return new PageResult<>(Collections.emptyList(), total);
        }

        List<ArticleVO> pageList = articleList.subList(fromIndex, toIndex);
        return new PageResult<>(pageList, total);
    }

    @Override
    public ArticleVO getArticleDetail(Long id, Long currentUserId) {
        // 1. 查询文章详情
        ArticleVO article = articleMapper.selectArticleDetail(id);
        if (article == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "文章不存在");
        }

        // 2. 增加阅读量（Redis异步更新）
        incrementViewCount(id);

        // 3. 设置用户交互状态
        if (currentUserId != null) {
            boolean liked = getLikeStatusFromRedis(id, currentUserId);
            boolean collected = getCollectStatusFromRedis(id, currentUserId);
            article.setLiked(liked);
            article.setCollected(collected);
        }

        return article;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public boolean addArticle(ArticleAddRequest request, Long userId) {
        Article article = new Article();
        // 复制除tags外的其他属性
        BeanUtils.copyProperties(request, article);
        // 处理tags，将List<String>转换为JSON字符串
        if (request.getTags() != null && !request.getTags().isEmpty()) {
            String tagsJson = JSONUtil.toJsonStr(request.getTags());
            article.setTags(tagsJson);
        } else {
            article.setTags(null);
        }
        article.setStatus("DRAFT");
        article.setAuditStatus("PENDING");
        article.setViewCount(0);
        article.setLikeCount(0);
        article.setCommentCount(0);
        article.setShareCount(0);
        article.setCollectCount(0);
        article.setCreateTime(new Date());
        article.setUpdateTime(new Date());

        return this.save(article);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public boolean updateArticle(Article article, Long userId) {
        Article existArticle = this.getById(article.getId());
        if (existArticle == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "文章不存在");
        }

        article.setUpdateTime(new Date());
        return this.updateById(article);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public boolean deleteArticle(Long id, Long userId) {
        Article article = this.getById(id);
        if (article == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "文章不存在");
        }

        return this.removeById(id);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public boolean publishArticle(Long id, Long userId) {
        Article article = this.getById(id);
        if (article == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "文章不存在");
        }

        // 记录更新前的状态
        log.info("发布文章前 - ID: {}, 状态: {}, 审核状态: {}",
                article.getId(), article.getStatus(), article.getAuditStatus());

        // 更新文章状态
        article.setStatus("PUBLISHED");
        article.setAuditStatus("APPROVED"); // 发布时自动审核通过
        article.setPublishTime(new Date());
        article.setUpdateTime(new Date());

        // 使用updateById更新
        boolean result = this.updateById(article);
        log.info("发布文章后 - ID: {}, 更新结果: {}, 状态: {}, 审核状态: {}",
                article.getId(), result, article.getStatus(), article.getAuditStatus());

        // 如果updateById失败，尝试使用QueryWrapper更新
        if (!result) {
            log.warn("updateById失败，尝试使用QueryWrapper更新");
            QueryWrapper<Article> updateWrapper = new QueryWrapper<>();
            updateWrapper.eq("id", id);
            Article updateArticle = new Article();
            updateArticle.setStatus("PUBLISHED");
            updateArticle.setAuditStatus("APPROVED");
            updateArticle.setPublishTime(article.getPublishTime());
            updateArticle.setUpdateTime(article.getUpdateTime());
            result = this.update(updateArticle, updateWrapper);
            log.info("使用QueryWrapper更新结果: {}", result);
        }

        return result;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public boolean offlineArticle(Long id, Long userId) {
        Article article = this.getById(id);
        if (article == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "文章不存在");
        }

        article.setStatus("OFFLINE");
        article.setUpdateTime(new Date());
        return this.updateById(article);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public boolean listedArticle(Long id, Long userId) {
        Article article = this.getById(id);
        if (article == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "文章不存在");
        }

        article.setStatus("PUBLISHED");
        article.setUpdateTime(new Date());
        return this.updateById(article);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public LikeResult likeArticle(Long articleId, Long userId) {
        // 1. 检查文章是否存在
        Article article = this.getById(articleId);
        if (article == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "文章不存在");
        }

        // 2. 查询点赞记录（不限制isDelete，查询所有记录）
        QueryWrapper<ArticleLike> wrapper = new QueryWrapper<>();
        wrapper.eq("articleId", articleId)
                .eq("userId", userId);
        ArticleLike existLike = articleLikeMapper.selectOne(wrapper);

        boolean newLikeStatus;
        int likeChange;

        if (existLike != null && existLike.getIsDelete() == 0) {
            // 已点赞 -> 取消点赞
            existLike.setIsDelete(1);
            articleLikeMapper.updateById(existLike);
            newLikeStatus = false;
            likeChange = -1;
            log.info("用户 {} 取消点赞文章 {}", userId, articleId);
        } else if (existLike != null && existLike.getIsDelete() == 1) {
            // 之前取消过 -> 重新点赞
            existLike.setIsDelete(0);
            articleLikeMapper.updateById(existLike);
            newLikeStatus = true;
            likeChange = 1;
            log.info("用户 {} 重新点赞文章 {}", userId, articleId);
        } else {
            // 首次点赞
            ArticleLike like = new ArticleLike();
            like.setArticleId(articleId);
            like.setUserId(userId);
            like.setCreateTime(new Date());
            like.setIsDelete(0);
            articleLikeMapper.insert(like);
            newLikeStatus = true;
            likeChange = 1;
            log.info("用户 {} 首次点赞文章 {}", userId, articleId);
        }

        // 3. 更新Redis缓存（点赞状态和计数）
        String likeKey = ARTICLE_LIKE_KEY_PREFIX + articleId;
        String countKey = ARTICLE_LIKE_COUNT_KEY_PREFIX + articleId;
        
        // 更新用户点赞状态
        redisTemplate.opsForHash().put(likeKey, userId.toString(), newLikeStatus);
        
        // 更新点赞计数（使用Redis increment，确保不会变成负数）
        Long currentCount = null;
        try {
            // 先获取当前Redis中的值
            String countStr = stringRedisTemplate.opsForValue().get(countKey);
            if (countStr != null) {
                long count = Long.parseLong(countStr);
                // 如果是取消点赞且当前计数>0，才减1
                if (likeChange == -1 && count > 0) {
                    currentCount = stringRedisTemplate.opsForValue().increment(countKey, -1);
                } else if (likeChange == 1) {
                    currentCount = stringRedisTemplate.opsForValue().increment(countKey, 1);
                } else {
                    currentCount = count;
                }
            } else {
                // Redis中没有值，从数据库获取并设置
                int dbCount = article.getLikeCount() != null ? article.getLikeCount() : 0;
                int newCount = Math.max(0, dbCount + likeChange);
                stringRedisTemplate.opsForValue().set(countKey, String.valueOf(newCount));
                currentCount = (long) newCount;
            }
        } catch (Exception e) {
            log.error("更新Redis点赞计数失败, articleId: {}", articleId, e);
            // Redis失败，直接从数据库计算
            int dbCount = article.getLikeCount() != null ? article.getLikeCount() : 0;
            currentCount = (long) Math.max(0, dbCount + likeChange);
        }

        // 4. 同步更新数据库点赞数（保证数据一致性）
        articleMapper.incrementLikeCount(articleId, likeChange);

        // 5. 清除热门文章缓存
        clearHotArticlesCache();

        // 6. 返回结果
        return new LikeResult(newLikeStatus, currentCount);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public CollectResult collectArticle(Long articleId, Long userId) {
        // 1. 检查文章是否存在
        Article article = this.getById(articleId);
        if (article == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "文章不存在");
        }

        // 2. 检查是否已收藏
        QueryWrapper<ArticleCollect> wrapper = new QueryWrapper<>();
        wrapper.eq("articleId", articleId)
                .eq("userId", userId);
        ArticleCollect existCollect = articleCollectMapper.selectOne(wrapper);

        boolean newCollectStatus;
        int collectChange;

        if (existCollect != null && existCollect.getIsDelete() == 0) {
            // 已收藏 -> 取消收藏
            existCollect.setIsDelete(1);
            articleCollectMapper.updateById(existCollect);
            newCollectStatus = false;
            collectChange = -1;
            log.info("用户 {} 取消收藏文章 {}", userId, articleId);
        } else if (existCollect != null && existCollect.getIsDelete() == 1) {
            // 之前取消过，现在重新收藏
            existCollect.setIsDelete(0);
            articleCollectMapper.updateById(existCollect);
            newCollectStatus = true;
            collectChange = 1;
            log.info("用户 {} 重新收藏文章 {}", userId, articleId);
        } else {
            // 首次收藏
            ArticleCollect collect = new ArticleCollect();
            collect.setArticleId(articleId);
            collect.setUserId(userId);
            collect.setCreateTime(new Date());
            collect.setIsDelete(0);
            articleCollectMapper.insert(collect);
            newCollectStatus = true;
            collectChange = 1;
            log.info("用户 {} 首次收藏文章 {}", userId, articleId);
        }

        // 3. 更新文章收藏数
        articleMapper.incrementCollectCount(articleId, collectChange);

        // 4. 更新Redis缓存
        String collectKey = ARTICLE_COLLECT_KEY_PREFIX + articleId;
        redisTemplate.opsForHash().put(collectKey, userId.toString(), newCollectStatus);

        // 5. 获取最新的收藏数
        Article updatedArticle = this.getById(articleId);
        Integer currentCollectCount = updatedArticle.getCollectCount();

        // 6. 返回收藏结果
        return new CollectResult(newCollectStatus, currentCollectCount);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public boolean cancelCollectArticle(Long articleId, Long userId) {
        // 1. 查询收藏记录
        QueryWrapper<ArticleCollect> wrapper = new QueryWrapper<>();
        wrapper.eq("articleId", articleId)
                .eq("userId", userId)
                .eq("isDelete", 0);
        ArticleCollect collect = articleCollectMapper.selectOne(wrapper);

        if (collect == null) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "未收藏该文章");
        }

        // 2. 删除收藏
        collect.setIsDelete(1);
        articleCollectMapper.updateById(collect);

        // 3. 更新文章收藏数
        articleMapper.incrementCollectCount(articleId, -1);

        // 4. 更新Redis缓存
        String collectKey = ARTICLE_COLLECT_KEY_PREFIX + articleId;
        redisTemplate.opsForHash().put(collectKey, userId.toString(), false);

        return true;
    }

    @Override
    public boolean getLikeStatus(Long articleId, Long userId) {
        return getLikeStatusFromRedis(articleId, userId);
    }

    @Override
    public boolean getCollectStatus(Long articleId, Long userId) {
        return getCollectStatusFromRedis(articleId, userId);
    }

    @Override
    public List<ArticleVO> getHotArticles(int limit, Long currentUserId) {
        // 1. 尝试从Redis获取
        try {
            String cached = (String) redisTemplate.opsForValue().get(HOT_ARTICLES_KEY);
            if (cached != null) {
                List<ArticleVO> articles = JSON.parseArray(cached, ArticleVO.class);
                if (currentUserId != null && !articles.isEmpty()) {
                    setUserInteractionStatus(articles, currentUserId);
                }
                return articles;
            }
        } catch (Exception e) {
            log.warn("获取缓存热门文章失败", e);
        }

        // 2. 从数据库查询
        List<Article> hotArticles = articleMapper.selectHotArticles(limit);
        List<ArticleVO> articleVOS = hotArticles.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());

        // 3. 设置用户交互状态
        if (currentUserId != null && !articleVOS.isEmpty()) {
            setUserInteractionStatus(articleVOS, currentUserId);
        }

        // 4. 缓存到Redis（1小时）
        try {
            redisTemplate.opsForValue().set(HOT_ARTICLES_KEY, JSON.toJSONString(articleVOS),
                    Duration.ofHours(1));
        } catch (Exception e) {
            log.warn("缓存热门文章失败", e);
        }

        return articleVOS;
    }

    @Override
    public List<ArticleVO> getRecommendArticles(int limit, Long currentUserId) {
        // 1. 尝试从Redis获取
        try {
            String cached = (String) redisTemplate.opsForValue().get(RECOMMEND_ARTICLES_KEY);
            if (cached != null) {
                List<ArticleVO> articles = JSON.parseArray(cached, ArticleVO.class);
                if (currentUserId != null && !articles.isEmpty()) {
                    setUserInteractionStatus(articles, currentUserId);
                }
                return articles;
            }
        } catch (Exception e) {
            log.warn("获取缓存推荐文章失败", e);
        }

        // 2. 从数据库查询
        QueryWrapper<Article> wrapper = new QueryWrapper<>();
        wrapper.eq("status", "PUBLISHED")
                .eq("auditStatus", "APPROVED")
                .eq("isRecommend", 1)
                .eq("isDelete", 0)
                .orderByDesc("publishTime")
                .last("LIMIT " + limit);
        List<Article> recommendArticles = this.list(wrapper);
        List<ArticleVO> articleVOS = recommendArticles.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());

        // 3. 设置用户交互状态
        if (currentUserId != null && !articleVOS.isEmpty()) {
            setUserInteractionStatus(articleVOS, currentUserId);
        }

        // 4. 缓存到Redis（1小时）
        try {
            redisTemplate.opsForValue().set(RECOMMEND_ARTICLES_KEY, JSON.toJSONString(articleVOS),
                    Duration.ofHours(1));
        } catch (Exception e) {
            log.warn("缓存推荐文章失败", e);
        }

        return articleVOS;
    }

    /**
     * 增加阅读量（Redis异步）
     */
    private void incrementViewCount(Long articleId) {
        String viewKey = ARTICLE_VIEW_KEY_PREFIX + articleId;
        stringRedisTemplate.opsForValue().increment(viewKey);
    }

    /**
     * 批量设置用户交互状态
     */
    private void setUserInteractionStatus(List<ArticleVO> articles, Long userId) {
        List<Long> articleIds = articles.stream()
                .map(ArticleVO::getId)
                .collect(Collectors.toList());

        // 批量查询点赞状态
        List<Long> likedArticleIds = articleLikeMapper.selectLikedArticleIds(userId, articleIds);
        Set<Long> likedSet = new HashSet<>(likedArticleIds);

        // 批量查询收藏状态
        List<Long> collectedArticleIds = articleCollectMapper.selectCollectedArticleIds(userId, articleIds);
        Set<Long> collectedSet = new HashSet<>(collectedArticleIds);

        // 设置状态
        articles.forEach(article -> {
            article.setLiked(likedSet.contains(article.getId()));
            article.setCollected(collectedSet.contains(article.getId()));
        });
    }

    /**
     * 从Redis获取点赞状态
     */
    private boolean getLikeStatusFromRedis(Long articleId, Long userId) {
        String likeKey = ARTICLE_LIKE_KEY_PREFIX + articleId;
        Boolean hasLiked = (Boolean) redisTemplate.opsForHash().get(likeKey, userId.toString());
        if (hasLiked != null) {
            return hasLiked;
        }

        // Redis中没有，从数据库查询
        QueryWrapper<ArticleLike> wrapper = new QueryWrapper<>();
        wrapper.eq("articleId", articleId)
                .eq("userId", userId)
                .eq("isDelete", 0);
        boolean liked = articleLikeMapper.selectCount(wrapper) > 0;

        // 写入Redis
        redisTemplate.opsForHash().put(likeKey, userId.toString(), liked);
        return liked;
    }

    /**
     * 从Redis获取收藏状态
     */
    private boolean getCollectStatusFromRedis(Long articleId, Long userId) {
        String collectKey = ARTICLE_COLLECT_KEY_PREFIX + articleId;
        Boolean hasCollected = (Boolean) redisTemplate.opsForHash().get(collectKey, userId.toString());
        if (hasCollected != null) {
            return hasCollected;
        }

        // Redis中没有，从数据库查询
        QueryWrapper<ArticleCollect> wrapper = new QueryWrapper<>();
        wrapper.eq("articleId", articleId)
                .eq("userId", userId)
                .eq("isDelete", 0);
        boolean collected = articleCollectMapper.selectCount(wrapper) > 0;

        // 写入Redis
        redisTemplate.opsForHash().put(collectKey, userId.toString(), collected);
        return collected;
    }

    /**
     * 安全地增加计数
     */
    private Long safeIncrement(String key, long delta) {
        try {
            return stringRedisTemplate.opsForValue().increment(key, delta);
        } catch (Exception e) {
            log.error("Redis increment failed for key: {}", key, e);
            return null;
        }
    }

    /**
     * 清除热门文章缓存
     */
    private void clearHotArticlesCache() {
        try {
            redisTemplate.delete(HOT_ARTICLES_KEY);
            redisTemplate.delete(RECOMMEND_ARTICLES_KEY);
        } catch (Exception e) {
            log.error("清除热门文章缓存失败", e);
        }
    }

    /**
     * 转换为VO
     */
    private ArticleVO convertToVO(Article article) {
        ArticleVO vo = new ArticleVO();
        BeanUtils.copyProperties(article, vo);
        return vo;
    }
}
