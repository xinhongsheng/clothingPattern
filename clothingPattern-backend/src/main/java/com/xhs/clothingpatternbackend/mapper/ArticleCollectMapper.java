package com.xhs.clothingpatternbackend.mapper;

import com.xhs.clothingpatternbackend.model.entity.ArticleCollect;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * @author 小辛
 * @description 针对表【article_collect(文章收藏表)】的数据库操作Mapper
 * @createDate 2025-11-26 16:42:01
 * @Entity com.xhs.clothingpatternbackend.model.entity.ArticleCollect
 */
public interface ArticleCollectMapper extends BaseMapper<ArticleCollect> {

    /**
     * 批量查询用户收藏的文章ID列表
     */
    default List<Long> selectCollectedArticleIds(@Param("userId") Long userId,
            @Param("articleIds") List<Long> articleIds) {
        com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<ArticleCollect> queryWrapper = new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<>();
        queryWrapper.eq(ArticleCollect::getUserId, userId)
                .eq(ArticleCollect::getIsDelete, 0)
                .in(ArticleCollect::getArticleId, articleIds)
                .select(ArticleCollect::getArticleId);

        List<Object> objects = this.selectObjs(queryWrapper);
        return objects.stream()
                .map(obj -> (Long) obj)
                .collect(java.util.stream.Collectors.toList());
    }
}
