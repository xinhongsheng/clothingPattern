package com.xhs.clothingpatternbackend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xhs.clothingpatternbackend.model.entity.PatternSimilarity;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 图案相似度 Mapper
 * @author xhs
 */
@Mapper
public interface PatternSimilarityMapper extends BaseMapper<PatternSimilarity> {

    /**
     * 清空相似度表（用于每次重新计算）
     */
    @Delete("TRUNCATE TABLE pattern_similarity")
    void truncateTable();

    /**
     * 获取与指定图案相似的图案ID列表（按相似度降序）
     */
    @Select("SELECT patternIdB FROM pattern_similarity WHERE patternIdA = #{patternId} ORDER BY similarity DESC LIMIT #{limit}")
    List<Long> selectSimilarPatternIds(@Param("patternId") Long patternId, @Param("limit") int limit);

    /**
     * 获取与指定图案相似的图案ID列表（双向查询）
     */
    @Select("SELECT DISTINCT CASE WHEN patternIdA = #{patternId} THEN patternIdB ELSE patternIdA END AS similarPatternId " +
            "FROM pattern_similarity " +
            "WHERE patternIdA = #{patternId} OR patternIdB = #{patternId} " +
            "ORDER BY similarity DESC LIMIT #{limit}")
    List<Long> selectSimilarPatternIdsBidirectional(@Param("patternId") Long patternId, @Param("limit") int limit);
}
