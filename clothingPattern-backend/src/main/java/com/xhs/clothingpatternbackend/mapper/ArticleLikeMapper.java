package com.xhs.clothingpatternbackend.mapper;

import com.xhs.clothingpatternbackend.model.entity.ArticleLike;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

/**
* @author 小辛
* @description 针对表【article_like(文章点赞表)】的数据库操作Mapper
* @createDate 2025-11-26 16:42:01
 * @Entity com.xhs.clothingpatternbackend.model.entity.ArticleLike
*/
public interface ArticleLikeMapper extends BaseMapper<ArticleLike> {

    /**
     * 查询所有记录（包括已删除的）
     */
    @Select("SELECT * FROM article_like WHERE articleId = #{articleId} AND userId = #{userId}")
    ArticleLike selectOneIncludeDeleted(@Param("articleId") Long articleId, @Param("userId") Long userId);

    /**
     * 更新点赞状态（直接更新isDelete字段，不受@TableLogic影响）
     */
    @Update("UPDATE article_like SET isDelete = #{isDelete} WHERE id = #{id}")
    int updateLikeStatus(@Param("id") Long id, @Param("isDelete") Integer isDelete);

}




