package com.xhs.clothingpatternbackend.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xhs.clothingpatternbackend.model.entity.ArticleLike;
import com.xhs.clothingpatternbackend.service.ArticleLikeService;
import com.xhs.clothingpatternbackend.mapper.ArticleLikeMapper;
import org.springframework.stereotype.Service;

import java.util.List;

/**
* @author 小辛
* @description 针对表【article_like(文章点赞表)】的数据库操作Service实现
* @createDate 2025-11-26 16:42:01
*/
@Service
public class ArticleLikeServiceImpl extends ServiceImpl<ArticleLikeMapper, ArticleLike>
    implements ArticleLikeService{
    /**
     * 批量查询用户点赞的文章ID列表
     */
    @Override
    public List<Long> selectLikedArticleIds(Long userId, List<Long> articleIds) {
        com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<ArticleLike> queryWrapper = new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<>();
        queryWrapper.eq(ArticleLike::getUserId, userId)
                .eq(ArticleLike::getIsDelete, 0)
                .in(ArticleLike::getArticleId, articleIds)
                .select(ArticleLike::getArticleId);

        List<Object> objects = this.listObjs(queryWrapper);
        return objects.stream()
                .map(obj -> (Long) obj)
                .collect(java.util.stream.Collectors.toList());
    }

}