package com.xhs.clothingpatternbackend.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xhs.clothingpatternbackend.model.entity.ArticleCollect;
import com.xhs.clothingpatternbackend.service.ArticleCollectService;
import com.xhs.clothingpatternbackend.mapper.ArticleCollectMapper;
import org.springframework.stereotype.Service;

import java.util.List;

/**
* @author 小辛
* @description 针对表【article_collect(文章收藏表)】的数据库操作Service实现
* @createDate 2025-11-26 16:42:01
*/
@Service
public class ArticleCollectServiceImpl extends ServiceImpl<ArticleCollectMapper, ArticleCollect>
    implements ArticleCollectService{

    @Override
    public List<Long> selectCollectedArticleIds(Long userId, List<Long> articleIds) {
        com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<ArticleCollect> queryWrapper = new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<>();
        queryWrapper.eq(ArticleCollect::getUserId, userId)
                .eq(ArticleCollect::getIsDelete, 0)
                .in(ArticleCollect::getArticleId, articleIds)
                .select(ArticleCollect::getArticleId);

        List<Object> objects = this.listObjs(queryWrapper);
        return objects.stream()
                .map(obj -> (Long) obj)
                .collect(java.util.stream.Collectors.toList());
    }

}




