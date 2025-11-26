# 文章模块BUG修复指南

## 🐛 问题描述

1. **ClassNotFoundException**: `com.alibaba.fastjson2.modules.ObjectReaderModule`
2. **文章不存在**: 数据库中没有文章数据
3. **发布后状态未更新**: `status` 和 `auditStatus` 没有正确更新

## ✅ 修复步骤

### 步骤1：修复 fastjson 依赖问题

**问题原因**: pom.xml 中使用了错误的 groupId

**修改文件**: `pom.xml`

**原代码**:
```xml
<dependency>
    <groupId>com.alibaba</groupId>
    <artifactId>fastjson</artifactId>
    <version>2.0.53</version>
</dependency>
```

**修改为**:
```xml
<dependency>
    <groupId>com.alibaba.fastjson2</groupId>
    <artifactId>fastjson2</artifactId>
    <version>2.0.53</version>
</dependency>
```

### 步骤2：修复 import 语句

**修改文件**: `ArticleServiceImpl.java`

**原代码**:
```java
import com.alibaba.fastjson.JSON;
```

**修改为**:
```java
import com.alibaba.fastjson2.JSON;
```

### 步骤3：修复发布文章逻辑

**修改文件**: `ArticleServiceImpl.java`

**原代码**:
```java
public boolean publishArticle(Long id, Long userId) {
    Article article = this.getById(id);
    if (article == null) {
        throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "文章不存在");
    }

    article.setStatus("PUBLISHED");
    article.setPublishTime(new Date());
    article.setUpdateTime(new Date());
    return this.updateById(article);
}
```

**修改为**:
```java
public boolean publishArticle(Long id, Long userId) {
    Article article = this.getById(id);
    if (article == null) {
        throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "文章不存在");
    }

    article.setStatus("PUBLISHED");
    article.setAuditStatus("APPROVED");  // ✅ 新增：自动审核通过
    article.setPublishTime(new Date());
    article.setUpdateTime(new Date());
    return this.updateById(article);
}
```

### 步骤4：插入测试数据

**执行SQL**: 在MySQL中执行 `快速插入文章测试数据.sql`

```bash
# 方法1：命令行执行
mysql -u root -p xhs_clothingPattern_db < 快速插入文章测试数据.sql

# 方法2：MySQL客户端中执行
# 打开 快速插入文章测试数据.sql 文件，复制内容到MySQL客户端执行
```

### 步骤5：重新编译和启动

1. **清理并重新编译**:
```bash
mvn clean install
```

2. **重启后端服务**

3. **验证修复**:
   - 访问 `http://localhost:8123/api/article/recommend` 应该返回文章列表
   - 访问文章管理页面应该能看到文章
   - 点击编辑应该正常打开编辑页面

## 📋 验证清单

- [ ] pom.xml 依赖已修改为 `com.alibaba.fastjson2`
- [ ] ArticleServiceImpl.java import 已修改
- [ ] publishArticle 方法已添加 `setAuditStatus("APPROVED")`
- [ ] 数据库中已插入测试文章
- [ ] Maven 重新编译成功
- [ ] 后端服务重启成功
- [ ] 推荐文章接口正常返回数据
- [ ] 文章管理页面正常显示
- [ ] 文章编辑功能正常

## 🔍 常见问题

### Q1: Maven 编译失败
**A**: 删除本地Maven仓库中的 fastjson 缓存：
```bash
rm -rf ~/.m2/repository/com/alibaba/fastjson
```
然后重新执行 `mvn clean install`

### Q2: 数据库插入失败
**A**: 检查数据库连接配置，确保：
- 数据库名称正确：`xhs_clothingPattern_db`
- 用户名密码正确
- 数据库服务已启动

### Q3: 文章列表仍然为空
**A**: 检查SQL执行结果：
```sql
-- 查看文章数量
SELECT COUNT(*) FROM article WHERE is_delete = 0;

-- 查看文章详情
SELECT id, title, status, audit_status FROM article WHERE is_delete = 0;
```

### Q4: 仍然报 ClassNotFoundException
**A**: 
1. 确认 pom.xml 修改已保存
2. 在IDE中刷新Maven项目（右键项目 -> Maven -> Reload Project）
3. 重新编译并重启

## 📝 修改文件清单

1. ✅ `pom.xml` - 修复 fastjson 依赖
2. ✅ `ArticleServiceImpl.java` - 修复 import 和发布逻辑
3. ✅ `快速插入文章测试数据.sql` - 新增测试数据SQL

## 🎯 预期结果

修复后应该能够：
1. ✅ 正常访问文章推荐接口
2. ✅ 在文章管理页面看到文章列表
3. ✅ 点击编辑正常打开编辑页面
4. ✅ 发布文章后状态正确更新为 `PUBLISHED` 和 `APPROVED`
5. ✅ 在前台文章页面看到已发布的文章
6. ✅ 点击文章详情正常查看内容

