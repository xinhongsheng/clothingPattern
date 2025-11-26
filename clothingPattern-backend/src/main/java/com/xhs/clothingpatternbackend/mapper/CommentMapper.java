package com.xhs.clothingpatternbackend.mapper;

import com.xhs.clothingpatternbackend.model.entity.Comment;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
* @author 小辛
* @description 针对表【comment(图案评论表)】的数据库操作Mapper
* @createDate 2025-11-26 15:12:15
* @Entity com.xhs.clothingpatternbackend.model.entity.Comment
*/
public interface CommentMapper extends BaseMapper<Comment> {

    /**
     * 查询图案的评论列表（带用户信息）
     */
    @Select("SELECT c.*, u.userName, u.userAvatar " +
            "FROM comment c " +
            "LEFT JOIN user u ON c.userId = u.id " +
            "WHERE c.patternId = #{patternId} " +
            "AND c.parentId IS NULL " +
            "AND c.isDelete = 0 " +
            "AND c.auditStatus = 'APPROVED' " +
            "ORDER BY c.topStatus DESC, c.createTime DESC " +
            "LIMIT #{offset}, #{limit}")
    List<Comment> selectCommentListByPattern(@Param("patternId") Long patternId, 
                                            @Param("offset") Long offset, 
                                            @Param("limit") Long limit);

    /**
     * 查询评论的回复列表（根据 rootId 查询所有回复）
     */
    @Select("SELECT c.*, u.userName, u.userAvatar, u2.userName as replyToUserName " +
            "FROM comment c " +
            "LEFT JOIN user u ON c.userId = u.id " +
            "LEFT JOIN user u2 ON c.replyToUserId = u2.id " +
            "WHERE c.rootId = #{rootId} " +
            "AND c.isDelete = 0 " +
            "AND c.auditStatus = 'APPROVED' " +
            "ORDER BY c.createTime ASC")
    List<Comment> selectRepliesByRootId(@Param("rootId") Long rootId);

    /**
     * 查询图案的评论数量
     */
    @Select("SELECT COUNT(*) FROM comment " +
            "WHERE patternId = #{patternId} " +
            "AND isDelete = 0 " +
            "AND auditStatus = 'APPROVED'")
    Integer selectCommentCountByPattern(@Param("patternId") Long patternId);

    /**
     * 查询图案的主评论数量
     */
    @Select("SELECT COUNT(*) FROM comment " +
            "WHERE patternId = #{patternId} " +
            "AND parentId IS NULL " +
            "AND isDelete = 0 " +
            "AND auditStatus = 'APPROVED'")
    Integer selectMainCommentCountByPattern(@Param("patternId") Long patternId);

    /**
     * 查询图案评论的总点赞数
     */
    @Select("SELECT COALESCE(SUM(likeCount), 0) FROM comment " +
            "WHERE patternId = #{patternId} " +
            "AND isDelete = 0 " +
            "AND auditStatus = 'APPROVED'")
    Integer selectTotalLikesByPattern(@Param("patternId") Long patternId);

    /**
     * 增加或减少回复数
     */
    @Update("UPDATE comment SET replyCount = replyCount + #{increment} " +
            "WHERE id = #{commentId}")
    int incrementReplyCount(@Param("commentId") Long commentId, 
                           @Param("increment") Integer increment);

    /**
     * 增加或减少点赞数
     */
    @Update("UPDATE comment SET likeCount = likeCount + #{increment} " +
            "WHERE id = #{commentId}")
    int incrementLikeCount(@Param("commentId") Long commentId, 
                          @Param("increment") Integer increment);
}




