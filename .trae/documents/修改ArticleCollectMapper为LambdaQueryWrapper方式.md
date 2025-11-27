## 修改计划

### 1. 修改ArticleCollectMapper.java
- 删除原有的@Select注解和XML SQL
- 使用LambdaQueryWrapper实现批量查询用户收藏的文章ID列表
- 保持方法签名不变，确保调用处不需要修改
- 使用BaseMapper的selectObjs方法获取结果
- 将结果转换为List<Long>类型

### 2. 技术实现
- 使用MyBatis-Plus的LambdaQueryWrapper构建查询条件
- 条件包括：userId匹配、isDelete=0、articleId在指定列表中
- 只查询articleId字段
- 使用selectObjs方法获取结果并转换类型

### 3. 预期结果
- 代码更简洁，不依赖XML
- 类型安全，避免SQL注入风险
- 保持原有方法签名，不影响调用处
- 实体类和数据库字段都是驼峰式，无需特殊映射处理

### 4. 文件路径
- `d:\myspace\上课代码\clothingPattern\clothingPattern-backend\src\main\java\com\xhs\clothingpatternbackend\mapper\ArticleCollectMapper.java`