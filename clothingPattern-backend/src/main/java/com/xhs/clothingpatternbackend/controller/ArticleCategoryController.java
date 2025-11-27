package com.xhs.clothingpatternbackend.controller;

import com.xhs.clothingpatternbackend.common.BaseResponse;
import com.xhs.clothingpatternbackend.common.ResultUtils;
import com.xhs.clothingpatternbackend.model.dto.article.CategoryAddRequest;
import com.xhs.clothingpatternbackend.model.dto.article.CategoryUpdateRequest;
import com.xhs.clothingpatternbackend.model.entity.ArticleCategory;
import com.xhs.clothingpatternbackend.model.vo.ArticleCategoryVO;
import com.xhs.clothingpatternbackend.service.ArticleCategoryService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 文章分类接口
 */
@RestController
@RequestMapping("/article/category")
@Slf4j
public class ArticleCategoryController {

    @Autowired
    private ArticleCategoryService categoryService;

    /**
     * 获取所有可用分类
     */
    @GetMapping("/list")
    public BaseResponse<List<ArticleCategoryVO>> getCategories() {
        List<ArticleCategoryVO> categories = categoryService.getEnabledCategories();
        return ResultUtils.success(categories);
    }

    /**
     * 获取所有分类（包括禁用的）
     */
    @GetMapping("/all")
    public BaseResponse<List<ArticleCategory>> getAllCategories() {
        List<ArticleCategory> categories = categoryService.getAllCategories();
        return ResultUtils.success(categories);
    }

    /**
     * 添加分类（管理员）
     */
    @PostMapping("/add")
    public BaseResponse<Boolean> addCategory(@Valid @RequestBody CategoryAddRequest request) {
        ArticleCategory category = new ArticleCategory();
        BeanUtils.copyProperties(request, category);
        boolean result = categoryService.addCategory(category);
        return ResultUtils.success(result);
    }

    /**
     * 更新分类（管理员）
     */
    @PostMapping("/update")
    public BaseResponse<Boolean> updateCategory(@Valid @RequestBody CategoryUpdateRequest request) {
        ArticleCategory category = new ArticleCategory();
        BeanUtils.copyProperties(request, category);
        boolean result = categoryService.updateCategory(category);
        return ResultUtils.success(result);
    }

    /**
     * 删除分类（管理员）
     */
    @PostMapping("/delete/{id}")
    public BaseResponse<Boolean> deleteCategory(@PathVariable Long id) {
        boolean result = categoryService.deleteCategory(id);
        return ResultUtils.success(result);
    }
}

