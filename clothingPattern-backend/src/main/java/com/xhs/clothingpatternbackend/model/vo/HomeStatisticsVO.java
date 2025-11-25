package com.xhs.clothingpatternbackend.model.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * 首页统计数据VO
 */
@Data
public class HomeStatisticsVO implements Serializable {

    /**
     * 热门风格分布 Map<风格名称, 图案数量>
     */
    private Map<String, Long> styleDistribution;

    /**
     * 活跃用户排行（前5名）
     */
    private List<ActiveUserVO> activeUsers;

    /**
     * 创作趋势（最近7天）
     */
    private List<TrendDataVO> trendData;

    /**
     * 总图案数
     */
    private Long totalPatterns;

    /**
     * 总用户数
     */
    private Long totalUsers;

    private static final long serialVersionUID = 1L;

    /**
     * 活跃用户VO
     */
    @Data
    public static class ActiveUserVO implements Serializable {
        /**
         * 用户信息
         */
        private UserVO user;

        /**
         * 图案数量
         */
        private Long patternCount;

        private static final long serialVersionUID = 1L;
    }

    /**
     * 趋势数据VO
     */
    @Data
    public static class TrendDataVO implements Serializable {
        /**
         * 日期（格式：yyyy-MM-dd）
         */
        private String date;

        /**
         * 图案数量
         */
        private Long count;

        private static final long serialVersionUID = 1L;
    }
}
