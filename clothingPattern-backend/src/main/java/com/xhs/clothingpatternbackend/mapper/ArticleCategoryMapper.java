package com.xhs.clothingpatternbackend.mapper;

import com.xhs.clothingpatternbackend.model.entity.ArticleCategory;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
* @author 小辛
* @description 针对表【article_category(文章分类表)】的数据库操作Mapper
* @createDate 2025-11-26 16:42:01
* @Entity com.xhs.clothingpatternbackend.model.entity.ArticleCategory
*/
public interface ArticleCategoryMapper extends BaseMapper<ArticleCategory> {

    /**
     * 查询所有启用的分类（按排序字段和创建时间排序）
     */
//    @Select("SELECT * FROM article_category WHERE status = 1 AND isDelete = 0 ORDER BY sortOrder DESC, createTime DESC")
//    List<ArticleCategory> selectEnabledCategories();
}




