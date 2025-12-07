package com.xhs.clothingpatternbackend.mapper;

import com.xhs.clothingpatternbackend.model.entity.Pattern;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

/**
 * @author 小辛
 * @description 针对表【pattern(服装图案表（智能图案生成模块核心表）)】的数据库操作Mapper
 * @createDate 2025-11-21 15:44:19
 * @Entity com.xhs.clothingpatternbackend.model.entity.Pattern
 */
public interface PatternMapper extends BaseMapper<Pattern> {
    /**
     * 查询最近活跃的图案（用于数据预热）
     * 
     * @param days  最近多少天
     * @param limit 限制数量
     * @return 图案列表
     */
    @Select("SELECT p.* FROM pattern p " +
            "WHERE p.isDelete = 0 AND p.auditStatus = 'APPROVED' " +
            "AND (p.updateTime >= DATE_SUB(NOW(), INTERVAL #{days} DAY) " +
            "OR p.likeCount > 0) " +
            "ORDER BY p.likeCount DESC, p.updateTime DESC " +
            "LIMIT #{limit}")
    List<Pattern> selectRecentActivePatterns(@Param("days") int days, @Param("limit") int limit);

    /**
     * 获取图案的当前点赞数（从MySQL）
     */
    @Select("SELECT likeCount FROM pattern WHERE id = #{patternId} AND isDelete = 0")
    Integer selectLikeCount(@Param("patternId") Long patternId);

    /**
     * 获取用户对图案的点赞状态（从MySQL）
     */
    @Select("SELECT COUNT(*) FROM user_like WHERE userId = #{userId} AND patternId = #{patternId} AND isDelete = 0")
    int selectUserLikeStatus(@Param("userId") Long userId, @Param("patternId") Long patternId);

}
