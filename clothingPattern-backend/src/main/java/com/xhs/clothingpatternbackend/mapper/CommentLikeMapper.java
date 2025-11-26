package com.xhs.clothingpatternbackend.mapper;

import com.xhs.clothingpatternbackend.model.entity.CommentLike;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
* @author 小辛
* @description 针对表【comment_like(评论点赞表)】的数据库操作Mapper
* @createDate 2025-11-26 15:12:15
* @Entity com.xhs.clothingpatternbackend.model.entity.CommentLike
*/
public interface CommentLikeMapper extends BaseMapper<CommentLike> {

    /**
     * 查询用户对评论的点赞状态
     */
    @Select("SELECT COUNT(*) FROM comment_like " +
            "WHERE userId = #{userId} " +
            "AND commentId = #{commentId} " +
            "AND isDelete = 0")
    Long countUserLike(@Param("userId") Long userId, 
                      @Param("commentId") Long commentId);

    /**
     * 批量查询用户对评论的点赞状态
     */
    @Select("<script>" +
            "SELECT commentId FROM comment_like " +
            "WHERE userId = #{userId} " +
            "AND commentId IN " +
            "<foreach collection='commentIds' item='commentId' open='(' separator=',' close=')'>" +
            "#{commentId}" +
            "</foreach>" +
            "AND isDelete = 0" +
            "</script>")
    List<Long> selectLikedCommentIds(@Param("userId") Long userId, 
                                     @Param("commentIds") List<Long> commentIds);
}




