package com.xhs.clothingpatternbackend.mapper;

import com.xhs.clothingpatternbackend.model.entity.ArticleLike;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
* @author 小辛
* @description 针对表【article_like(文章点赞表)】的数据库操作Mapper
* @createDate 2025-11-26 16:42:01
* @Entity com.xhs.clothingpatternbackend.model.entity.ArticleLike
*/
public interface ArticleLikeMapper extends BaseMapper<ArticleLike> {

    /**
     * 批量查询用户点赞的文章ID列表
     */
    @Select("<script>" +
            "SELECT articleId FROM article_like " +
            "WHERE userId = #{userId} AND isDelete = 0 " +
            "AND articleId IN " +
            "<foreach collection='articleIds' item='id' open='(' separator=',' close=')'>" +
            "#{id}" +
            "</foreach>" +
            "</script>")
    List<Long> selectLikedArticleIds(@Param("userId") Long userId, @Param("articleIds") List<Long> articleIds);
}




