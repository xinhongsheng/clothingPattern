package com.xhs.clothingpatternbackend.mapper;

import com.xhs.clothingpatternbackend.model.dto.article.ArticleQueryRequest;
import com.xhs.clothingpatternbackend.model.entity.Article;
import com.xhs.clothingpatternbackend.model.vo.ArticleVO;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;
import java.util.Map;

/**
 * @author 小辛
 * @description 针对表【article(文章资讯表)】的数据库操作Mapper
 * @createDate 2025-11-26 16:42:01
 * @Entity com.xhs.clothingpatternbackend.model.entity.Article
 */
public interface ArticleMapper extends BaseMapper<Article> {

    /**
     * 增加阅读量（+1）
     */
    @Update("UPDATE article SET viewCount = viewCount + 1 WHERE id = #{id}")
    int incrementViewCount(@Param("id") Long id);

    /**
     * 批量增加阅读量
     */
    @Update("UPDATE article SET viewCount = viewCount + #{increment} WHERE id = #{id}")
    int incrementViewCountBatch(@Param("id") Long id, @Param("increment") int increment);

    /**
     * 更新点赞数（增加或减少），确保点赞数不会变成负数
     */
    @Update("UPDATE article SET likeCount = CASE WHEN likeCount + #{increment} < 0 THEN 0 ELSE likeCount + #{increment} END WHERE id = #{id}")
    int incrementLikeCount(@Param("id") Long id, @Param("increment") int increment);

    /**
     * 更新收藏数（增加或减少），确保收藏数不会变成负数
     */
    @Update("UPDATE article SET collectCount = CASE WHEN collectCount + #{increment} < 0 THEN 0 ELSE collectCount + #{increment} END WHERE id = #{id}")
    int incrementCollectCount(@Param("id") Long id, @Param("increment") int increment);

    /**
     * 获取热门文章
     */
    @Select("SELECT * FROM article WHERE status = 'PUBLISHED' AND auditStatus = 'APPROVED' AND isDelete = 0 " +
            "ORDER BY viewCount DESC, likeCount DESC LIMIT #{limit}")
    List<Article> selectHotArticles(@Param("limit") int limit);

    /**
     * 更新文章热门状态
     */
    @Update("UPDATE article SET isHot = #{isHot} WHERE id = #{id}")
    int updateHotStatus(@Param("id") Long id, @Param("isHot") int isHot);
}
