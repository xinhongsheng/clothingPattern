package com.xhs.clothingpatternbackend.mapper;

import com.xhs.clothingpatternbackend.model.entity.UserLike;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
* @author 小辛
* @description 针对表【like(图案点赞表)】的数据库操作Mapper
* @createDate 2025-11-25 13:04:21
* @Entity com.xhs.clothingpatternbackend.model.entity.UserLike
*/
public interface LikeMapper extends BaseMapper<UserLike> {
    /**
     * 批量查询用户点赞状态
     */
    @Select({
            "<script>",
            "SELECT patternId FROM user_like",
            "WHERE userId = #{userId} AND patternId IN",
            "<foreach collection='patternIds' item='patternId' open='(' separator=',' close=')'>",
            "#{patternId}",
            "</foreach>",
            "AND isDelete = 0",
            "</script>"
    })
    List<Long> selectLikedPatternIds(@Param("userId") Long userId, @Param("patternIds") List<Long> patternIds);

    /**
     * 获取图案的所有点赞用户ID
     */
    @Select("SELECT userId FROM user_like WHERE patternId = #{patternId} AND isDelete = 0")
    List<Long> selectLikedUserIds(@Param("patternId") Long patternId);

    /**
     * 查询用户是否对图案点赞（返回有效记录数）
     */
    @Select("SELECT COUNT(*) FROM user_like WHERE userId = #{userId} AND patternId = #{patternId} AND isDelete = 0")
    Long countValidLike(@Param("userId") Long userId, @Param("patternId") Long patternId);

    /**
     * 查询用户点赞记录（包括已删除的记录，用于同步数据）
     * 注意：这个方法会查询所有记录，包括 isDelete = 1 的记录
     */
    @Select("SELECT id, userId, patternId, createTime, isDelete FROM user_like WHERE userId = #{userId} AND patternId = #{patternId} LIMIT 1")
    UserLike selectOneIncludeDeleted(@Param("userId") Long userId, @Param("patternId") Long patternId);

    /**
     * 更新点赞状态（直接更新 isDelete 字段，绕过 @TableLogic）
     * @param id 点赞记录ID
     * @param isDelete 0-有效，1-已删除
     * @return 更新的行数
     */
    @org.apache.ibatis.annotations.Update("UPDATE user_like SET isDelete = #{isDelete} WHERE id = #{id}")
    int updateLikeStatus(@Param("id") Long id, @Param("isDelete") Integer isDelete);

    /**
     * INSERT IGNORE 原子插入（解决并发问题）
     * @param id 点赞记录ID
     * @param userId 用户ID
     * @param patternId 图案ID
     * @param isDelete 删除标记（0-有效，1-已删除）
     * @return 影响的行数（0=已存在，1=插入成功）
     */
    int insertIgnore(@Param("id") Long id, @Param("userId") Long userId, 
                     @Param("patternId") Long patternId, @Param("isDelete") Integer isDelete);
}




