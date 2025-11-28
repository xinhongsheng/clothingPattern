# Midjourney 数据库字段问题修复

## 🐛 问题描述

保存 MJ 生成的图案到数据库时出现错误：

```
java.sql.SQLException: Field 'patternUrl' doesn't have a default value
```

**SQL 语句**：
```sql
INSERT INTO pattern ( id, userId, patternName, description, generationType, generationParams, auditStatus )  
VALUES ( ?, ?, ?, ?, ?, ?, ? )
```

**问题**：SQL 插入语句中缺少 `patternUrl` 字段！

## 🔍 原因分析

### 1. MyBatis-Plus 自动忽略了 null 字段

MyBatis-Plus 默认配置下，如果字段值为 `null`，会自动忽略该字段，不包含在 INSERT 语句中。

### 2. MJ API 响应中 URL 可能为空

可能的情况：
- `rawImageUrl` 为 null
- `imageUrl` 为 null
- 或者两者都为 null

### 3. 数据库字段定义

```sql
`patternUrl` varchar(512) NOT NULL COMMENT '生成图案的最终URL'
```

该字段定义为 `NOT NULL` 且没有默认值，所以必须提供值。

## ✅ 解决方案

### 修复1：添加空值检查和默认值处理

**文件**：`MJController.java`

```java
// 检查必要的字段
String rawImageUrl = mjResponse.getRawImageUrl();
String imageUrl = mjResponse.getImageUrl();

// 如果原始图片URL为空，使用缩略图URL
if (StringUtils.isBlank(rawImageUrl)) {
    rawImageUrl = imageUrl;
}

// 如果两个URL都为空，抛出异常
ThrowUtils.throwIf(StringUtils.isBlank(rawImageUrl), ErrorCode.SYSTEM_ERROR, 
    "图片URL为空，生成失败");

// 保存到数据库
pattern.setPatternUrl(rawImageUrl); // 确保不为空
pattern.setThumbUrl(StringUtils.isNotBlank(imageUrl) ? imageUrl : rawImageUrl);
```

**改进点**：
- ✅ 检查 `rawImageUrl` 是否为空
- ✅ 如果为空，使用 `imageUrl` 作为备用
- ✅ 如果两个都为空，抛出明确的异常
- ✅ 确保 `patternUrl` 字段一定有值

### 修复2：添加调试日志

```java
log.info("MJ响应详情 - taskId: {}, imageId: {}, rawImageUrl: {}, imageUrl: {}", 
    mjResponse.getTaskId(), mjResponse.getImageId(), 
    mjResponse.getRawImageUrl(), mjResponse.getImageUrl());
```

**作用**：
- 帮助排查 MJ API 返回的具体内容
- 确认哪些字段为空

## 🔧 其他可能的解决方案

### 方案1：修改数据库字段（不推荐）

```sql
ALTER TABLE `pattern` 
MODIFY COLUMN `patternUrl` varchar(512) NULL DEFAULT NULL 
COMMENT '生成图案的最终URL';
```

**缺点**：
- ❌ 破坏数据完整性
- ❌ 允许存储无效数据
- ❌ 不符合业务逻辑

### 方案2：使用默认占位符（不推荐）

```java
if (StringUtils.isBlank(rawImageUrl)) {
    rawImageUrl = "https://placeholder.com/default.png";
}
```

**缺点**：
- ❌ 存储无效URL
- ❌ 用户看到错误的图片
- ❌ 不如直接抛出异常

### 方案3：当前方案（推荐）✅

```java
// 检查并使用备用值
if (StringUtils.isBlank(rawImageUrl)) {
    rawImageUrl = imageUrl;
}

// 如果都为空，抛出异常
ThrowUtils.throwIf(StringUtils.isBlank(rawImageUrl), 
    ErrorCode.SYSTEM_ERROR, "图片URL为空，生成失败");
```

**优点**：
- ✅ 确保数据有效性
- ✅ 明确的错误提示
- ✅ 符合业务逻辑

## 📊 字段映射关系

| Pattern 字段 | MJ 响应字段 | 处理逻辑 |
|-------------|------------|----------|
| `patternUrl` | `rawImageUrl` | 优先使用，如果为空则使用 `imageUrl` |
| `thumbUrl` | `imageUrl` | 优先使用，如果为空则使用 `rawImageUrl` |

### 逻辑流程

```
1. 获取 rawImageUrl 和 imageUrl
   ↓
2. rawImageUrl 为空？
   ├─ 是 → 使用 imageUrl
   └─ 否 → 使用 rawImageUrl
   ↓
3. 最终值仍为空？
   ├─ 是 → 抛出异常
   └─ 否 → 保存到数据库
```

## 🧪 测试验证

### 测试1：正常情况

**MJ 响应**：
```json
{
  "rawImageUrl": "https://cdn.com/image.png",
  "imageUrl": "https://cdn.com/thumb.png",
  "success": true
}
```

**结果**：
- `patternUrl` = "https://cdn.com/image.png" ✅
- `thumbUrl` = "https://cdn.com/thumb.png" ✅

### 测试2：rawImageUrl 为空

**MJ 响应**：
```json
{
  "rawImageUrl": null,
  "imageUrl": "https://cdn.com/thumb.png",
  "success": true
}
```

**结果**：
- `patternUrl` = "https://cdn.com/thumb.png" ✅
- `thumbUrl` = "https://cdn.com/thumb.png" ✅

### 测试3：两个都为空

**MJ 响应**：
```json
{
  "rawImageUrl": null,
  "imageUrl": null,
  "success": true
}
```

**结果**：
- 抛出异常："图片URL为空，生成失败" ✅
- 不会插入数据库 ✅

## 📝 相关代码位置

### MJController.java

```java
// 第100-130行
try {
    MJImagineResponse mjResponse = mjGenImage.imagine(request);
    
    // 检查URL
    String rawImageUrl = mjResponse.getRawImageUrl();
    String imageUrl = mjResponse.getImageUrl();
    
    if (StringUtils.isBlank(rawImageUrl)) {
        rawImageUrl = imageUrl;
    }
    
    ThrowUtils.throwIf(StringUtils.isBlank(rawImageUrl), 
        ErrorCode.SYSTEM_ERROR, "图片URL为空，生成失败");
    
    pattern.setPatternUrl(rawImageUrl);
    pattern.setThumbUrl(StringUtils.isNotBlank(imageUrl) ? imageUrl : rawImageUrl);
    
    patternService.save(pattern);
}
```

## ⚠️ 注意事项

1. **MJ API 响应格式**
   - 确保 API 正常返回图片URL
   - 检查 token 是否有效
   - 确认网络连接正常

2. **数据库字段约束**
   - `patternUrl` 字段为 NOT NULL
   - 必须提供有效的URL值
   - 不能使用空字符串

3. **错误处理**
   - URL 为空时会抛出明确异常
   - 不会插入无效数据
   - 用户会收到清晰的错误提示

4. **日志记录**
   - 记录完整的 MJ 响应信息
   - 便于排查问题
   - 包含 taskId、imageId、URL 等

## 🔍 排查步骤

如果仍然出现问题，按以下步骤排查：

### 1. 检查日志

查看是否有以下日志：

```
MJ响应详情 - taskId: xxx, imageId: xxx, rawImageUrl: xxx, imageUrl: xxx
```

### 2. 验证 MJ API 响应

直接调用 `/api/mj/imagine` 接口（不保存），查看响应：

```bash
curl -X POST http://localhost:8123/api/mj/imagine \
  -H "Content-Type: application/json" \
  -d '{"prompt": "test"}'
```

### 3. 检查响应字段

确认响应中包含：
- ✅ `success: true`
- ✅ `rawImageUrl` 不为空
- ✅ `imageUrl` 不为空

### 4. 检查数据库字段

```sql
SHOW FULL COLUMNS FROM `pattern` WHERE Field = 'patternUrl';
```

确认：
- ✅ Type: varchar(512)
- ✅ Null: NO
- ✅ Default: NULL（或其他值）

## 📚 相关文档

- `Midjourney快速开始.md` - 快速入门
- `Midjourney接口使用说明.md` - API 文档
- `Midjourney超时问题解决方案.md` - 超时问题

## ✅ 修复总结

| 修改项 | 状态 |
|--------|------|
| 添加 URL 空值检查 | ✅ |
| 使用备用 URL 逻辑 | ✅ |
| 添加异常处理 | ✅ |
| 添加调试日志 | ✅ |
| 确保字段不为空 | ✅ |

**现在可以重新测试了！** 🚀

---

**更新时间**：2025-11-28  
**版本**：1.0

