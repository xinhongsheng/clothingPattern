# 将MyBatis XML查询转换为MyBatis-Plus实现

## 任务分析

需要将两个MyBatis XML查询转换为MyBatis-Plus的Java代码实现：
1. `selectArticleList` - 分页查询文章列表，支持多条件过滤和排序
2. `selectArticleDetail` - 查询文章详情

## 技术准备

### 现有代码结构
- **Article实体类** - 对应article表
- **ArticleCategory实体类** - 对应article_category表
- **ArticleQueryRequest** - 查询参数类
- **ArticleVO** - 视图对象
- **ArticleMapper** - Mapper接口，已继承BaseMapper
- **ArticleService** - 服务接口，已继承IService
- **ArticleServiceImpl** - 服务实现

### MyBatis-Plus功能
- **QueryWrapper/LambdaQueryWrapper** - 构建查询条件
- **Page** - 分页对象
- **联表查询** - 使用MyBatis-Plus的Join功能
- **排序** - 使用orderBy方法

## 实现步骤

### 1. 修改ArticleMapper.java

- 删除XML中对应的方法声明
- 确保继承BaseMapper<Article>

### 2. 实现selectArticleList查询

**功能**：分页查询文章列表，支持多条件过滤和排序

**实现思路**：
1. 使用LambdaQueryWrapper构建查询条件
2. 实现联表查询，关联article_category表获取categoryName
3. 实现多条件过滤（状态、审核状态、分类ID、关键词、标签等）
4. 实现动态排序
5. 使用Page对象实现分页

**代码实现**：
```java
// 在ArticleServiceImpl.java中实现
public PageResult<ArticleVO> getArticleList(ArticleQueryRequest query, Long currentUserId) {
    // 构建分页对象
    Page<Article> page = new Page<>(query.getPageNum(), query.getPageSize());
    
    // 构建查询条件
    LambdaQueryWrapper<Article> queryWrapper = new LambdaQueryWrapper<>();
    
    // 基础条件：未删除
    queryWrapper.eq(Article::getIsDelete, 0);
    
    // 状态过滤
    if (StrUtil.isNotBlank(query.getStatus())) {
        queryWrapper.eq(Article::getStatus, query.getStatus());
    }
    
    // 审核状态过滤
    if (StrUtil.isNotBlank(query.getAuditStatus())) {
        queryWrapper.eq(Article::getAuditStatus, query.getAuditStatus());
    }
    
    // 分类ID过滤
    if (query.getCategoryId() != null) {
        queryWrapper.eq(Article::getCategoryId, query.getCategoryId());
    }
    
    // 关键词过滤
    if (StrUtil.isNotBlank(query.getKeyword())) {
        String keyword = query.getKeyword();
        queryWrapper.and(wrapper -> wrapper.like(Article::getTitle, keyword)
            .or().like(Article::getSummary, keyword)
            .or().like(Article::getContent, keyword));
    }
    
    // 标签过滤
    if (query.getTags() != null && !query.getTags().isEmpty()) {
        queryWrapper.and(wrapper -> {
            for (String tag : query.getTags()) {
                wrapper.or().like(Article::getTags, tag);
            }
        });
    }
    
    // 置顶状态过滤
    if (query.getIsTop() != null) {
        queryWrapper.eq(Article::getIsTop, query.getIsTop());
    }
    
    // 热门状态过滤
    if (query.getIsHot() != null) {
        queryWrapper.eq(Article::getIsHot, query.getIsHot());
    }
    
    // 推荐状态过滤
    if (query.getIsRecommend() != null) {
        queryWrapper.eq(Article::getIsRecommend, query.getIsRecommend());
    }
    
    // 排序
    queryWrapper.orderByDesc(Article::getIsTop); // 始终优先按置顶排序
    
    // 动态排序
    if ("publishTime".equals(query.getSortField())) {
        if ("desc".equals(query.getSortOrder())) {
            queryWrapper.orderByDesc(Article::getPublishTime);
        } else {
            queryWrapper.orderByAsc(Article::getPublishTime);
        }
    } else if ("viewCount".equals(query.getSortField())) {
        if ("desc".equals(query.getSortOrder())) {
            queryWrapper.orderByDesc(Article::getViewCount);
        } else {
            queryWrapper.orderByAsc(Article::getViewCount);
        }
    } else if ("likeCount".equals(query.getSortField())) {
        if ("desc".equals(query.getSortOrder())) {
            queryWrapper.orderByDesc(Article::getLikeCount);
        } else {
            queryWrapper.orderByAsc(Article::getLikeCount);
        }
    } else if ("createTime".equals(query.getSortField())) {
        if ("desc".equals(query.getSortOrder())) {
            queryWrapper.orderByDesc(Article::getCreateTime);
        } else {
            queryWrapper.orderByAsc(Article::getCreateTime);
        }
    } else {
        // 默认按发布时间降序
        queryWrapper.orderByDesc(Article::getPublishTime);
    }
    
    // 执行查询
    Page<Article> articlePage = this.page(page, queryWrapper);
    
    // 转换为VO并关联分类名称
    List<ArticleVO> articleVOList = articlePage.getRecords().stream().map(article -> {
        ArticleVO articleVO = new ArticleVO();
        BeanUtils.copyProperties(article, articleVO);
        // 查询分类名称
        ArticleCategory category = articleCategoryService.getById(article.getCategoryId());
        if (category != null) {
            articleVO.setCategoryName(category.getCategoryName());
        }
        return articleVO;
    }).collect(Collectors.toList());
    
    // 构建分页结果
    PageResult<ArticleVO> pageResult = new PageResult<>();
    pageResult.setList(articleVOList);
    pageResult.setTotal(articlePage.getTotal());
    pageResult.setPageNum(articlePage.getCurrent());
    pageResult.setPageSize(articlePage.getSize());
    
    return pageResult;
}
```

### 3. 实现selectArticleDetail查询

**功能**：查询文章详情

**实现思路**：
1. 使用LambdaQueryWrapper构建查询条件
2. 实现联表查询，关联article_category表获取categoryName
3. 根据ID查询文章

**代码实现**：
```java
// 在ArticleServiceImpl.java中实现
public ArticleVO getArticleDetail(Long id, Long currentUserId) {
    // 查询文章
    Article article = this.getById(id);
    if (article == null || article.getIsDelete() == 1) {
        throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "文章不存在");
    }
    
    // 转换为VO
    ArticleVO articleVO = new ArticleVO();
    BeanUtils.copyProperties(article, articleVO);
    
    // 查询分类名称
    ArticleCategory category = articleCategoryService.getById(article.getCategoryId());
    if (category != null) {
        articleVO.setCategoryName(category.getCategoryName());
    }
    
    return articleVO;
}
```

### 4. 删除XML中的查询语句

删除`ArticleMapper.xml`中的`selectArticleList`和`selectArticleDetail`查询语句。

## 注意事项

1. **联表查询**：使用MyBatis-Plus的Join功能或手动查询关联表
2. **条件构建**：使用LambdaQueryWrapper构建类型安全的查询条件
3. **分页处理**：使用Page对象实现分页
4. **排序处理**：实现动态排序逻辑
5. **VO转换**：手动转换实体类到VO
6. **分类名称关联**：查询ArticleCategory获取categoryName

## 预期效果

- 功能保持不变，查询逻辑从XML迁移到Java代码
- 代码更加类型安全，减少SQL注入风险
- 便于维护和扩展
- 充分利用MyBatis-Plus的功能

## 测试建议

1. 测试文章列表查询，验证多条件过滤和排序
2. 测试文章详情查询，验证分类名称关联
3. 测试分页功能
4. 测试标签查询功能
5. 测试关键词搜索功能

## 技术栈

- Java
- Spring Boot
- MyBatis-Plus
- Lombok

## 实现时间

预计需要30-45分钟完成所有修改和测试。