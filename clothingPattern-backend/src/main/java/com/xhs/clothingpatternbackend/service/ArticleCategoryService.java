package com.xhs.clothingpatternbackend.service;

import com.xhs.clothingpatternbackend.model.entity.ArticleCategory;
import com.baomidou.mybatisplus.extension.service.IService;
import com.xhs.clothingpatternbackend.model.vo.ArticleCategoryVO;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
* @author 小辛
* @description 针对表【article_category(文章分类表)】的数据库操作Service
* @createDate 2025-11-26 16:42:01
*/
public interface ArticleCategoryService extends IService<ArticleCategory> {

    List<ArticleCategoryVO> getEnabledCategories();

    List<ArticleCategory> getAllCategories();

    @Transactional(rollbackFor = Exception.class)
    boolean addCategory(ArticleCategory category);

    @Transactional(rollbackFor = Exception.class)
    boolean updateCategory(ArticleCategory category);

    @Transactional(rollbackFor = Exception.class)
    boolean deleteCategory(Long id);
}
