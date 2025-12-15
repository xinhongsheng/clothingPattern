package com.xhs.clothingpatternbackend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xhs.clothingpatternbackend.model.entity.UserBehavior;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 用户行为 Mapper
 * @author xhs
 */
@Mapper
public interface UserBehaviorMapper extends BaseMapper<UserBehavior> {

    /**
     * 检查用户是否已经存在相同行为记录
     */
    @Select("SELECT COUNT(*) FROM user_behavior WHERE userId = #{userId} AND patternId = #{patternId} AND actionType = #{actionType}")
    int countByUserAndPatternAndAction(@Param("userId") Long userId,
                                        @Param("patternId") Long patternId,
                                        @Param("actionType") String actionType);

    /**
     * 获取用户交互过的所有图案ID列表
     */
    @Select("SELECT DISTINCT patternId FROM user_behavior WHERE userId = #{userId}")
    List<Long> selectPatternIdsByUserId(@Param("userId") Long userId);
}
