# Midjourney JSON 字段映射问题修复

## 🐛 问题描述

MJ API 返回的 JSON 数据解析后所有字段都是 null：

```
MJ响应详情 - taskId: null, imageId: null, rawImageUrl: null, imageUrl: null
```

但实际的 API 响应中有数据：

```json
{
  "image_url": "https://platform.cdn.zhishuyun.com/midjourney/xxx.png?imageMogr2/thumbnail/!50p",
  "raw_image_url": "https://platform.cdn.zhishuyun.com/midjourney/xxx.png",
  "image_id": "1443831187658244096",
  "task_id": "ed35383d-2915-4ee0-ac28-d403d36e0b41",
  "success": true
}
```

## 🔍 原因分析

### JSON 字段命名风格不匹配

| API 返回（下划线） | Java 对象（驼峰） | 映射结果 |
|-------------------|------------------|----------|
| `image_url` | `imageUrl` | ❌ null |
| `raw_image_url` | `rawImageUrl` | ❌ null |
| `image_id` | `imageId` | ❌ null |
| `task_id` | `taskId` | ❌ null |
| `image_width` | `imageWidth` | ❌ null |
| `raw_image_width` | `rawImageWidth` | ❌ null |
| `sub_image_urls` | `subImageUrls` | ❌ null |
| `trace_id` | `traceId` | ❌ null |

**问题**：FastJSON2 默认不会自动转换下划线命名到驼峰命名，导致字段无法映射。

## ✅ 解决方案

### 添加 @JSONField 注解

在 `MJImagineResponse` 类的每个字段上添加 `@JSONField(name = "xxx")` 注解：

```java
import com.alibaba.fastjson2.annotation.JSONField;

@Data
public class MJImagineResponse implements Serializable {
    
    @JSONField(name = "image_url")
    private String imageUrl;
    
    @JSONField(name = "raw_image_url")
    private String rawImageUrl;
    
    @JSONField(name = "image_id")
    private String imageId;
    
    @JSONField(name = "task_id")
    private String taskId;
    
    @JSONField(name = "image_width")
    private Integer imageWidth;
    
    @JSONField(name = "raw_image_width")
    private Integer rawImageWidth;
    
    @JSONField(name = "image_height")
    private Integer imageHeight;
    
    @JSONField(name = "raw_image_height")
    private Integer rawImageHeight;
    
    @JSONField(name = "sub_image_urls")
    private List<String> subImageUrls;
    
    @JSONField(name = "trace_id")
    private String traceId;
    
    // 其他字段...
}
```

### 完整的字段映射表

| JSON 字段（API） | Java 字段 | @JSONField 注解 |
|-----------------|-----------|----------------|
| `image_url` | `imageUrl` | `@JSONField(name = "image_url")` |
| `image_width` | `imageWidth` | `@JSONField(name = "image_width")` |
| `image_height` | `imageHeight` | `@JSONField(name = "image_height")` |
| `actions` | `actions` | `@JSONField(name = "actions")` |
| `raw_image_url` | `rawImageUrl` | `@JSONField(name = "raw_image_url")` |
| `raw_image_width` | `rawImageWidth` | `@JSONField(name = "raw_image_width")` |
| `raw_image_height` | `rawImageHeight` | `@JSONField(name = "raw_image_height")` |
| `sub_image_urls` | `subImageUrls` | `@JSONField(name = "sub_image_urls")` |
| `progress` | `progress` | `@JSONField(name = "progress")` |
| `image_id` | `imageId` | `@JSONField(name = "image_id")` |
| `task_id` | `taskId` | `@JSONField(name = "task_id")` |
| `success` | `success` | `@JSONField(name = "success")` |
| `trace_id` | `traceId` | `@JSONField(name = "trace_id")` |

## 🎯 修复效果

### 修复前

```java
MJImagineResponse mjResponse = JSON.parseObject(responseBody, MJImagineResponse.class);
// 结果：所有字段都是 null
System.out.println(mjResponse.getImageUrl());    // null ❌
System.out.println(mjResponse.getRawImageUrl()); // null ❌
System.out.println(mjResponse.getTaskId());      // null ❌
```

### 修复后

```java
MJImagineResponse mjResponse = JSON.parseObject(responseBody, MJImagineResponse.class);
// 结果：正确映射
System.out.println(mjResponse.getImageUrl());    
// "https://platform.cdn.zhishuyun.com/midjourney/xxx.png?imageMogr2/thumbnail/!50p" ✅

System.out.println(mjResponse.getRawImageUrl()); 
// "https://platform.cdn.zhishuyun.com/midjourney/xxx.png" ✅

System.out.println(mjResponse.getTaskId());      
// "ed35383d-2915-4ee0-ac28-d403d36e0b41" ✅
```

## 🔧 其他解决方案（不推荐）

### 方案1：全局配置 FastJSON（不推荐）

```java
// 全局启用驼峰转换
JSON.config(JSONReader.Feature.SupportSmartMatch);
```

**缺点**：
- ❌ 影响所有 JSON 解析
- ❌ 可能导致其他地方出现问题
- ❌ 不够明确

### 方案2：修改字段名为下划线（不推荐）

```java
private String image_url;
private String raw_image_url;
```

**缺点**：
- ❌ 不符合 Java 命名规范
- ❌ IDE 会报警告
- ❌ 代码可读性差

### 方案3：使用 @JSONField 注解（推荐）✅

```java
@JSONField(name = "image_url")
private String imageUrl;
```

**优点**：
- ✅ 明确的字段映射
- ✅ 符合 Java 命名规范
- ✅ 不影响其他代码
- ✅ 易于维护

## 📝 修改的文件

- ✅ `MJImagineResponse.java` - 添加所有字段的 @JSONField 注解

## 🧪 测试验证

### 测试步骤

1. 重启应用
2. 调用生成接口
3. 查看日志

### 预期日志

**修复前**：
```
MJ响应详情 - taskId: null, imageId: null, rawImageUrl: null, imageUrl: null
```

**修复后**：
```
MJ响应详情 - taskId: ed35383d-2915-4ee0-ac28-d403d36e0b41, 
             imageId: 1443831187658244096, 
             rawImageUrl: https://platform.cdn.zhishuyun.com/midjourney/xxx.png, 
             imageUrl: https://platform.cdn.zhishuyun.com/midjourney/xxx.png?imageMogr2/thumbnail/!50p
```

## 💡 知识点

### FastJSON2 字段映射规则

1. **默认行为**：字段名必须完全匹配
   - JSON: `imageUrl` → Java: `imageUrl` ✅
   - JSON: `image_url` → Java: `imageUrl` ❌

2. **使用 @JSONField**：显式指定映射
   - `@JSONField(name = "image_url")` → 强制映射 ✅

3. **SupportSmartMatch 特性**：智能匹配（全局）
   - 自动转换下划线和驼峰
   - 不推荐使用（影响范围太大）

### 命名风格对比

| 风格 | 示例 | 常用场景 |
|------|------|----------|
| 驼峰命名 | `imageUrl` | Java、JavaScript |
| 下划线命名 | `image_url` | Python、数据库、部分 API |
| 短横线命名 | `image-url` | CSS、HTML 属性 |

## ⚠️ 注意事项

1. **所有下划线字段都需要注解**
   - 不要遗漏任何字段
   - 否则该字段会是 null

2. **注解名称必须与 JSON 完全匹配**
   - 区分大小写
   - 包括下划线位置

3. **导入正确的注解类**
   ```java
   import com.alibaba.fastjson2.annotation.JSONField;
   ```
   不要导入错误的包（如 fastjson1 的注解）

4. **序列化和反序列化都会生效**
   - 反序列化：JSON → Java 对象
   - 序列化：Java 对象 → JSON

## 📚 相关文档

- `Midjourney快速开始.md` - 快速入门
- `Midjourney接口使用说明.md` - API 文档
- `Midjourney超时问题解决方案.md` - 超时问题
- `Midjourney数据库字段问题修复.md` - 数据库问题

## ✅ 修复总结

| 修改项 | 状态 |
|--------|------|
| 添加 @JSONField 注解 | ✅ |
| 映射所有下划线字段 | ✅ |
| 导入 FastJSON2 注解 | ✅ |
| 保持 Java 命名规范 | ✅ |

**现在可以重新测试了！** 🚀

---

**更新时间**：2025-11-28  
**版本**：1.0

