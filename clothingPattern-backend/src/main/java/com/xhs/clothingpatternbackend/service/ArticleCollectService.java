package com.xhs.clothingpatternbackend.service;

import com.xhs.clothingpatternbackend.model.entity.ArticleCollect;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
* @author 小辛
* @description 针对表【article_collect(文章收藏表)】的数据库操作Service
* @createDate 2025-11-26 16:42:01
*/
public interface ArticleCollectService extends IService<ArticleCollect> {

    /**
     * 批量查询用户收藏的文章ID列表
     */
    List<Long> selectCollectedArticleIds(Long userId, List<Long> articleIds);

}
