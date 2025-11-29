package com.xhs.clothingpatternbackend.service;

import com.xhs.clothingpatternbackend.model.entity.ArticleLike;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
* @author 小辛
* @description 针对表【article_like(文章点赞表)】的数据库操作Service
* @createDate 2025-11-26 16:42:01
*/
public interface ArticleLikeService extends IService<ArticleLike> {

    /**
     * 批量查询用户点赞的文章ID列表
     */
    List<Long> selectLikedArticleIds(Long userId, List<Long> articleIds);

}