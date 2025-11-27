package com.xhs.clothingpatternbackend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xhs.clothingpatternbackend.exception.BusinessException;
import com.xhs.clothingpatternbackend.exception.ErrorCode;
import com.xhs.clothingpatternbackend.mapper.ArticleCategoryMapper;
import com.xhs.clothingpatternbackend.model.entity.ArticleCategory;
import com.xhs.clothingpatternbackend.model.vo.ArticleCategoryVO;
import com.xhs.clothingpatternbackend.service.ArticleCategoryService;
import com.xhs.clothingpatternbackend.utils.CategoryIconUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 文章分类服务实现
 */
@Service
@Slf4j
public class ArticleCategoryServiceImpl extends ServiceImpl<ArticleCategoryMapper, ArticleCategory>
        implements ArticleCategoryService {

    @Autowired
    private ArticleCategoryMapper categoryMapper;
    
    @Autowired
    private CategoryIconUtils categoryIconUtils;


    /**
     * 获取所有启用的分类
     */
    @Override
    public List<ArticleCategoryVO> getEnabledCategories() {
        LambdaQueryWrapper<ArticleCategory> queryWrapper = new LambdaQueryWrapper<ArticleCategory>()
                .eq(ArticleCategory::getStatus, 1)
                .eq(ArticleCategory::getIsDelete, 0)
                .orderByDesc(ArticleCategory::getSortOrder, ArticleCategory::getCreateTime);
        List<ArticleCategory> articleCategories = categoryMapper.selectList(queryWrapper);
        return articleCategories.stream()
                .map(entity -> {
                    ArticleCategoryVO vo = new ArticleCategoryVO();
                    BeanUtils.copyProperties(entity, vo);
                    return vo;
                })
                .collect(Collectors.toList());
//        return categoryMapper.selectEnabledCategories();
    }

    @Override
    public List<ArticleCategory> getAllCategories() {
        QueryWrapper<ArticleCategory> wrapper = new QueryWrapper<>();
        wrapper.orderByDesc("sortOrder", "createTime");
        return this.list(wrapper);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public boolean addCategory(ArticleCategory category) {
        // 检查分类名称是否已存在
        QueryWrapper<ArticleCategory> wrapper = new QueryWrapper<>();
        wrapper.eq("categoryName", category.getCategoryName())
                .eq("isDelete", 0);
        long count = this.count(wrapper);
        if (count > 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "分类名称已存在");
        }
        
        // 处理分类图标上传
        if (category.getIcon() != null && category.getIcon().startsWith("data:image")) {
            String cosUrl = categoryIconUtils.uploadCategoryIcon(category.getIcon());
            category.setIcon(cosUrl);
        }

        category.setCreateTime(new Date());
        category.setUpdateTime(new Date());
        if (category.getStatus() == null) {
            category.setStatus(1);
        }
        if (category.getSortOrder() == null) {
            category.setSortOrder(0);
        }

        return this.save(category);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public boolean updateCategory(ArticleCategory category) {
        ArticleCategory existCategory = this.getById(category.getId());
        if (existCategory == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "分类不存在");
        }

        // 检查分类名称是否重复
        if (!existCategory.getCategoryName().equals(category.getCategoryName())) {
            QueryWrapper<ArticleCategory> wrapper = new QueryWrapper<>();
            wrapper.eq("categoryName", category.getCategoryName())
                    .eq("isDelete", 0)
                    .ne("id", category.getId());
            long count = this.count(wrapper);
            if (count > 0) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "分类名称已存在");
            }
        }
        
        // 处理分类图标上传
        if (category.getIcon() != null && category.getIcon().startsWith("data:image")) {
            String cosUrl = categoryIconUtils.uploadCategoryIcon(category.getIcon());
            category.setIcon(cosUrl);
        }

        category.setUpdateTime(new Date());
        return this.updateById(category);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public boolean deleteCategory(Long id) {
        ArticleCategory category = this.getById(id);
        if (category == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "分类不存在");
        }

        return this.removeById(id);
    }
}
