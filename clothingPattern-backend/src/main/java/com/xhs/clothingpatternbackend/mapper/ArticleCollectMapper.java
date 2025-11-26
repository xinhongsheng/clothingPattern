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
    @Select("<script>" +
            "SELECT articleId FROM article_collect " +
            "WHERE userId = #{userId} AND isDelete = 0 " +
            "AND articleId IN " +
            "<foreach collection='articleIds' item='id' open='(' separator=',' close=')'>" +
            "#{id}" +
            "</foreach>" +
            "</script>")
    List<Long> selectCollectedArticleIds(@Param("userId") Long userId, @Param("articleIds") List<Long> articleIds);
}




