## 问题分析
1. **编译错误原因**：`PageResult`类只有`list`和`total`两个字段，没有`pageNum`和`pageSize`字段及对应的setter方法，导致调用`setPageNum()`和`setPageSize()`方法时编译失败。
2. **代码不一致**：其他地方（如`CommentServiceImpl`）使用`PageResult`时只传递`list`和`total`两个参数，没有设置`pageNum`和`pageSize`。
3. **分页需求**：前端需要分页信息（total），直接返回List会导致前端无法获取总记录数，影响分页功能。

## 修复计划
1. **修复编译错误**：修改`getArticleList`方法，移除对不存在方法的调用，使用正确的方式初始化`PageResult`。
2. **保持代码一致性**：与`CommentServiceImpl`的实现保持一致，只设置`list`和`total`字段。
3. **继续使用PageResult**：保留`PageResult`作为返回类型，因为前端需要分页信息。

## 具体修改
1. **修改`ArticleServiceImpl.java`中的`getArticleList`方法**：
   - 移除第5-6步中对`setPageNum()`和`setPageSize()`方法的调用
   - 使用构造函数直接初始化`PageResult`，只传递`list`和`total`参数
   - 保持其他逻辑不变

## 预期效果
1. 编译成功，不再出现"Cannot resolve method"错误
2. 代码结构更加清晰，与其他服务的实现保持一致
3. 前端仍然可以获取到完整的分页信息
4. 代码可维护性更高，符合单一职责原则