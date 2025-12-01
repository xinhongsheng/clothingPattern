package com.xhs.clothingpatternbackend.mapper;

import com.xhs.clothingpatternbackend.model.entity.ArticleCollect;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

/**
* @author 小辛
* @description 针对表【article_collect(文章收藏表)】的数据库操作Mapper
* @createDate 2025-11-26 16:42:01
        * @Entity com.xhs.clothingpatternbackend.model.entity.ArticleCollect
*/
public interface ArticleCollectMapper extends BaseMapper<ArticleCollect> {

    /**
     * 查询所有记录（包括已删除的）
     */
    @Select("SELECT * FROM article_collect WHERE articleId = #{articleId} AND userId = #{userId}")
    ArticleCollect selectOneIncludeDeleted(@Param("articleId") Long articleId, @Param("userId") Long userId);

    /**
     * 更新收藏状态（直接更新isDelete字段，不受@TableLogic影响）
     */
    @Update("UPDATE article_collect SET isDelete = #{isDelete} WHERE id = #{id}")
    int updateCollectStatus(@Param("id") Long id, @Param("isDelete") Integer isDelete);

    /**
     * 查询有效记录（isDelete=0）
     */
    @Select("SELECT * FROM article_collect WHERE articleId = #{articleId} AND userId = #{userId} AND isDelete = 0")
    ArticleCollect selectValidCollect(@Param("articleId") Long articleId, @Param("userId") Long userId);

}
