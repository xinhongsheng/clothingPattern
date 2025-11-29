## 问题分析
1. 当前`ArticleCollectMapper`中定义了一个默认方法`selectCollectedArticleIds`，用于批量查询用户收藏的文章ID列表
2. 这个方法应该由Service层提供，而不是Mapper层
3. 需要将这个方法移到`ArticleCollectService`接口中，并在`ArticleCollectServiceImpl`中实现
4. 移除`ArticleCollectMapper`中的默认方法

## 修复计划
1. **修改`ArticleCollectService.java`**：
   - 添加`selectCollectedArticleIds`方法声明
   - 方法签名与Mapper中的默认方法保持一致

2. **修改`ArticleCollectServiceImpl.java`**：
   - 实现`selectCollectedArticleIds`方法
   - 使用MyBatis-Plus的LambdaQueryWrapper构建查询条件
   - 使用selectObjs方法查询结果
   - 将结果转换为List<Long>

3. **修改`ArticleCollectMapper.java`**：
   - 移除默认方法`selectCollectedArticleIds`
   - 保持其他代码不变

## 具体修改
1. **修改`ArticleCollectService.java`**：
   - 添加方法声明：`List<Long> selectCollectedArticleIds(Long userId, List<Long> articleIds);`

2. **修改`ArticleCollectServiceImpl.java`**：
   - 实现`selectCollectedArticleIds`方法，使用LambdaQueryWrapper构建查询条件
   - 使用selectObjs方法查询结果
   - 将结果转换为List<Long>

3. **修改`ArticleCollectMapper.java`**：
   - 移除默认方法`selectCollectedArticleIds`

## 预期效果
1. 代码结构更加清晰，符合分层设计原则
2. Service层提供业务逻辑，Mapper层只负责数据库操作
3. 代码可维护性更高
4. 编译成功，没有语法错误

## 注意事项
1. 确保方法签名与原来的默认方法保持一致
2. 保持查询逻辑不变
3. 确保代码风格与现有代码一致
4. 测试功能是否正常工作