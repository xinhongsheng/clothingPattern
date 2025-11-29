package com.xhs.clothingpatternbackend.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xhs.clothingpatternbackend.model.dto.article.ArticleAddRequest;
import com.xhs.clothingpatternbackend.model.dto.article.ArticleQueryRequest;
import com.xhs.clothingpatternbackend.model.entity.Article;
import com.baomidou.mybatisplus.extension.service.IService;
import com.xhs.clothingpatternbackend.model.vo.ArticleVO;
import com.xhs.clothingpatternbackend.model.vo.CollectResult;
import com.xhs.clothingpatternbackend.model.vo.LikeResultVO;
import com.xhs.clothingpatternbackend.model.vo.PageResult;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * @author 小辛
 * @description 针对表【article(文章资讯表)】的数据库操作Service
 * @createDate 2025-11-26 16:42:01
 */
public interface ArticleService extends IService<Article> {

    /**
     * 获取文章列表（分页）
     */
    Page<ArticleVO> getArticleList(ArticleQueryRequest query, Long currentUserId);

    /**
     * 获取文章详情
     */
    ArticleVO getArticleDetail(Long id, Long currentUserId);

    /**
     * 添加文章
     */
    @Transactional(rollbackFor = Exception.class)
    boolean addArticle(ArticleAddRequest request, Long userId);

    /**
     * 更新文章
     */
    @Transactional(rollbackFor = Exception.class)
    boolean updateArticle(Article article, Long userId);

    /**
     * 删除文章
     */
    @Transactional(rollbackFor = Exception.class)
    boolean deleteArticle(Long id, Long userId);

    /**
     * 发布文章
     */
    @Transactional(rollbackFor = Exception.class)
    boolean publishArticle(Long id, Long userId);

    /**
     * 下架文章
     */
    @Transactional(rollbackFor = Exception.class)
    boolean offlineArticle(Long id, Long userId);

    @Transactional(rollbackFor = Exception.class)
    boolean listedArticle(Long id, Long userId);

    /**
     * 点赞文章
     */
    @Transactional(rollbackFor = Exception.class)
    LikeResultVO likeArticle(Long articleId, Long userId);

    /**
     * 收藏文章（切换收藏状态）
     */
    @Transactional(rollbackFor = Exception.class)
    CollectResult collectArticle(Long articleId, Long userId);

    /**
     * 取消收藏文章
     */
    @Transactional(rollbackFor = Exception.class)
    boolean cancelCollectArticle(Long articleId, Long userId);

    /**
     * 获取点赞状态
     */
    boolean getLikeStatus(Long articleId, Long userId);

    /**
     * 获取收藏状态
     */
    boolean getCollectStatus(Long articleId, Long userId);

    /**
     * 获取热门文章
     */
    List<ArticleVO> getHotArticles(int limit, Long currentUserId);

    /**
     * 获取推荐文章
     */
    List<ArticleVO> getRecommendArticles(int limit, Long currentUserId);

    /**
     * 获取我的收藏文章列表
     */
    List<ArticleVO> getMyCollectArticles(Long userId);

    /**
     * 获取查询条件
     */
    QueryWrapper<Article> getQueryWrapper(ArticleQueryRequest articleQueryRequest);

    /**
     * 将Article列表转换为ArticleVO列表
     */
    List<ArticleVO> getArticleVOList(List<Article> articleList);
}
