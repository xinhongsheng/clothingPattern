# Midjourney 图案保存使用指南

## 📋 方案说明

我们选择**复用现有的 `pattern` 表**来存储 Midjourney 生成的图案，而不是新建表。

### 为什么不新建表？

✅ **优势**：
1. **统一管理**：所有图案在一个表中，便于统一展示、搜索、管理
2. **功能复用**：审核、点赞、收藏等功能无需重复开发
3. **表结构完美适配**：现有字段完全满足需求
4. **扩展性好**：未来接入其他 AI 服务也可以使用同一套结构

❌ **新建表的缺点**：
1. 数据分散，管理复杂
2. 功能重复开发（审核、点赞等）
3. 前端需要维护多套逻辑
4. 数据统计和分析困难

## 🗄️ 数据库字段映射

### Pattern 表字段与 MJ 数据的对应关系

| Pattern 字段 | MJ 数据 | 说明 |
|-------------|---------|------|
| `userId` | 登录用户ID | 创建者 |
| `patternName` | 自动生成 | "MJ-" + prompt前30字符 |
| `description` | prompt | 用户输入的提示词 |
| `generationType` | **"MJ_GENERATED"** | 新增的生成类型 |
| `patternUrl` | rawImageUrl | 原始高清图片URL |
| `thumbUrl` | imageUrl | 缩略图URL |
| `generationParams` | 完整MJ响应 | JSON格式存储所有信息 |
| `auditStatus` | "PENDING" | 默认待审核 |
| `referenceImageUrl` | null | MJ不需要参考图 |
| `fileSize` | null | 可选 |
| `fileType` | "image/png" | 可选 |
| `style` | null | 可从prompt提取 |
| `season` | null | 可选 |
| `targetAudience` | null | 可选 |

### generationParams 存储的 MJ 响应信息

```json
{
  "imageUrl": "缩略图URL",
  "imageWidth": 1024,
  "imageHeight": 1024,
  "actions": ["upsample1", "upsample2", "upsample3", "upsample4", "reroll", "variation1", "variation2", "variation3", "variation4"],
  "rawImageUrl": "原始图片URL",
  "rawImageWidth": 2048,
  "rawImageHeight": 2048,
  "subImageUrls": [],
  "progress": 100,
  "imageId": "1443792963657269248",
  "taskId": "b1efab91-dda2-4dd7-b2fc-8694799beabf",
  "success": true,
  "traceId": "065e947f-9eb4-4f6e-80ff-8d822271d751"
}
```

**重要**：`taskId` 和 `imageId` 保存在这里，用于后续的 upsample、variation 等操作。

## 🔧 已实现的功能

### 1. 枚举类更新

**文件**：`GenerationTypeEnum.java`

```java
public enum GenerationTypeEnum {
    TEXT_GENERATED("文字生成", "TEXT_GENERATED"),
    IMAGE_REFERENCED("图片参考生成", "IMAGE_REFERENCED"),
    MJ_GENERATED("Midjourney生成", "MJ_GENERATED");  // ✅ 新增
}
```

### 2. 新增接口

#### 接口1：生成图片（不保存）
```
POST /api/mj/imagine
```

**用途**：仅调用 MJ API 生成图片，返回结果，不保存到数据库

**使用场景**：
- 测试提示词效果
- 预览生成结果
- 不需要保存的临时生成

**请求示例**：
```json
{
  "prompt": "The clothing pattern of the capybara Lulu",
  "action": "generate"
}
```

**响应示例**：
```json
{
  "code": 0,
  "data": {
    "imageUrl": "缩略图URL",
    "rawImageUrl": "原始图片URL",
    "taskId": "b1efab91-dda2-4dd7-b2fc-8694799beabf",
    "imageId": "1443792963657269248",
    "success": true,
    ...
  },
  "message": "ok"
}
```

#### 接口2：生成图片并保存（推荐）⭐
```
POST /api/mj/generate
```

**用途**：调用 MJ API 生成图片，并自动保存到 pattern 表

**使用场景**：
- 正式生成图案
- 需要保存到图案库
- 需要后续审核、展示、管理

**请求示例**：
```json
{
  "prompt": "The clothing pattern of the capybara Lulu",
  "action": "generate"
}
```

**响应示例**：
```json
{
  "code": 0,
  "data": 1992847245649223683,  // 返回保存的图案ID
  "message": "ok"
}
```

**注意**：
- ✅ 需要登录
- ✅ 自动关联当前用户
- ✅ 默认状态为待审核（PENDING）
- ✅ 完整的 MJ 响应信息保存在 generationParams 字段

## 🚀 使用流程

### 流程1：生成并保存图案（推荐）

```
1. 用户登录
   ↓
2. 调用 POST /api/mj/generate
   ↓
3. 后端调用 MJ API 生成图片
   ↓
4. 自动保存到 pattern 表
   ↓
5. 返回图案ID
   ↓
6. 前端跳转到图案详情页或列表页
```

### 流程2：先预览再保存

```
1. 用户输入提示词
   ↓
2. 调用 POST /api/mj/imagine（预览）
   ↓
3. 用户确认满意
   ↓
4. 调用 POST /api/mj/generate（保存）
   ↓
5. 保存到图案库
```

### 流程3：后续操作（Upsample/Variation）

```
1. 从 pattern 表查询图案
   ↓
2. 从 generationParams 字段获取 taskId 和 imageId
   ↓
3. 调用 POST /api/mj/action 执行操作
   ↓
4. 可选：将新生成的图片再次保存为新图案
```

## 📝 代码示例

### 前端调用示例（Vue3 + TypeScript）

```typescript
// 生成并保存图案
async function generateAndSavePattern(prompt: string) {
  try {
    const response = await fetch('/api/mj/generate', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify({
        prompt: prompt,
        action: 'generate'
      })
    });
    
    const result = await response.json();
    
    if (result.code === 0) {
      const patternId = result.data;
      console.log('图案保存成功，ID:', patternId);
      
      // 跳转到图案详情页
      router.push(`/pattern/${patternId}`);
    }
  } catch (error) {
    console.error('生成失败:', error);
  }
}

// 查询 MJ 生成的图案列表
async function getMJPatterns() {
  const response = await fetch('/api/pattern/list', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json'
    },
    body: JSON.stringify({
      generationType: 'MJ_GENERATED',
      current: 1,
      pageSize: 10
    })
  });
  
  return await response.json();
}

// 执行 Upsample 操作
async function upsamplePattern(patternId: number, index: number) {
  // 1. 获取图案信息
  const pattern = await getPatternById(patternId);
  
  // 2. 解析 generationParams 获取 taskId 和 imageId
  const mjInfo = JSON.parse(pattern.generationParams);
  
  // 3. 调用 action 接口
  const response = await fetch('/api/mj/action', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json'
    },
    body: JSON.stringify({
      taskId: mjInfo.taskId,
      imageId: mjInfo.imageId,
      action: `upsample${index}` // upsample1, upsample2, upsample3, upsample4
    })
  });
  
  return await response.json();
}
```

### 后端查询示例（Java）

```java
// 查询所有 MJ 生成的图案
@GetMapping("/mj/list")
public BaseResponse<Page<PatternVO>> listMJPatterns(PatternQueryRequest queryRequest) {
    queryRequest.setGenerationType("MJ_GENERATED");
    Page<Pattern> patternPage = patternService.page(
        new Page<>(queryRequest.getCurrent(), queryRequest.getPageSize()),
        patternService.getQueryWrapper(queryRequest)
    );
    return ResultUtils.success(patternPage);
}

// 获取图案的 MJ 信息
public MJImagineResponse getMJInfo(Long patternId) {
    Pattern pattern = patternService.getById(patternId);
    if (pattern != null && "MJ_GENERATED".equals(pattern.getGenerationType())) {
        String params = pattern.getGenerationParams();
        return JSON.parseObject(params, MJImagineResponse.class);
    }
    return null;
}
```

## 🔍 数据查询示例

### SQL 查询

```sql
-- 查询所有 MJ 生成的图案
SELECT * FROM pattern 
WHERE generationType = 'MJ_GENERATED' 
ORDER BY createTime DESC;

-- 查询某个用户的 MJ 图案
SELECT * FROM pattern 
WHERE userId = 1 AND generationType = 'MJ_GENERATED'
ORDER BY createTime DESC;

-- 统计各类型图案数量
SELECT generationType, COUNT(*) as count 
FROM pattern 
GROUP BY generationType;

-- 查询待审核的 MJ 图案
SELECT * FROM pattern 
WHERE generationType = 'MJ_GENERATED' 
  AND auditStatus = 'PENDING'
ORDER BY createTime DESC;

-- 从 generationParams 中提取 taskId（MySQL 5.7+）
SELECT 
  id,
  patternName,
  JSON_EXTRACT(generationParams, '$.taskId') as taskId,
  JSON_EXTRACT(generationParams, '$.imageId') as imageId
FROM pattern 
WHERE generationType = 'MJ_GENERATED';
```

## 📊 数据统计示例

```sql
-- MJ 图案统计
SELECT 
  COUNT(*) as total,
  SUM(CASE WHEN auditStatus = 'APPROVED' THEN 1 ELSE 0 END) as approved,
  SUM(CASE WHEN auditStatus = 'PENDING' THEN 1 ELSE 0 END) as pending,
  SUM(CASE WHEN auditStatus = 'REJECTED' THEN 1 ELSE 0 END) as rejected,
  SUM(likeCount) as totalLikes
FROM pattern 
WHERE generationType = 'MJ_GENERATED';

-- 按日期统计 MJ 图案生成量
SELECT 
  DATE(createTime) as date,
  COUNT(*) as count
FROM pattern 
WHERE generationType = 'MJ_GENERATED'
GROUP BY DATE(createTime)
ORDER BY date DESC;
```

## ⚠️ 注意事项

1. **登录验证**：`/api/mj/generate` 接口需要用户登录
2. **审核流程**：生成的图案默认为待审核状态，需要管理员审核后才能展示
3. **图片存储**：当前使用 MJ 提供的 CDN URL，如需保存到自己的 COS，需要额外开发下载和上传功能
4. **taskId 和 imageId**：保存在 `generationParams` 字段中，用于后续操作
5. **图案命名**：自动截取 prompt 前30字符，建议用户可以修改

## 🎯 后续优化建议

### 功能优化
1. **图片下载到 COS**：将 MJ 生成的图片下载并上传到自己的 COS
2. **批量生成**：支持批量提示词生成
3. **提示词模板**：提供常用的提示词模板
4. **智能命名**：根据 prompt 内容智能生成图案名称
5. **标签提取**：从 prompt 中自动提取 style、season 等标签

### 界面优化
1. **生成进度**：显示生成进度（可以通过轮询或 WebSocket）
2. **操作按钮**：在图案详情页显示 Upsample、Variation 等操作按钮
3. **历史记录**：显示用户的生成历史
4. **提示词优化**：提供提示词优化建议

### 数据优化
1. **缓存机制**：缓存常用的图案数据
2. **CDN 加速**：使用 CDN 加速图片加载
3. **懒加载**：图案列表使用懒加载
4. **预加载**：预加载下一页数据

## 📞 技术支持

如有问题，请参考：
1. `Midjourney快速开始.md` - 快速入门
2. `Midjourney接口使用说明.md` - 详细 API 文档
3. `sql/update_add_mj_generation_type.sql` - 数据库更新脚本

---

**更新时间**：2025-11-28  
**版本**：1.0

