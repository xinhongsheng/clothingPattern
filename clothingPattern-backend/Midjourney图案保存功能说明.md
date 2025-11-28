# Midjourney 图案保存功能说明

## 📝 更新内容

### 1. 枚举类更新
**文件**：`GenerationTypeEnum.java`

新增 `MJ_GENERATED` 类型：
```java
MJ_GENERATED("Midjourney生成", "MJ_GENERATED")
```

### 2. Controller 更新
**文件**：`MJController.java`

#### 新增接口：生成并保存
```
POST /api/mj/generate
```

**功能**：
- 调用 Midjourney API 生成图片
- 自动保存到 `pattern` 表
- 返回保存的图案ID

**请求参数**：
```json
{
  "prompt": "The clothing pattern of the capybara Lulu",
  "action": "generate"
}
```

**响应**：
```json
{
  "code": 0,
  "data": 1992847245649223683,  // 图案ID
  "message": "ok"
}
```

**特点**：
- ✅ 需要用户登录
- ✅ 自动关联当前用户
- ✅ 默认状态为待审核（PENDING）
- ✅ 完整的 MJ 响应保存在 `generationParams` 字段

#### 原有接口：仅生成
```
POST /api/mj/imagine
```

**功能**：
- 仅调用 Midjourney API
- 返回生成结果
- 不保存到数据库

**用途**：
- 测试提示词
- 预览效果
- 临时生成

### 3. 数据库映射

#### Pattern 表字段使用

| 字段 | 值 | 说明 |
|------|-----|------|
| `userId` | 当前登录用户ID | 创建者 |
| `patternName` | "MJ-" + prompt前30字符 | 自动生成 |
| `description` | 完整的 prompt | 提示词 |
| `generationType` | "MJ_GENERATED" | 生成类型 |
| `patternUrl` | rawImageUrl | 原始高清图 |
| `thumbUrl` | imageUrl | 缩略图 |
| `generationParams` | 完整MJ响应JSON | 包含taskId、imageId等 |
| `auditStatus` | "PENDING" | 待审核 |
| `referenceImageUrl` | null | 不需要 |
| `fileSize` | null | 可选 |
| `fileType` | null | 可选 |

#### generationParams 存储内容示例

```json
{
  "imageUrl": "https://platform.cdn.zhishuyun.com/midjourney/xxx.png?imageMogr2/thumbnail/!50p",
  "imageWidth": 1024,
  "imageHeight": 1024,
  "actions": ["upsample1", "upsample2", "upsample3", "upsample4", "reroll", "variation1", "variation2", "variation3", "variation4"],
  "rawImageUrl": "https://platform.cdn.zhishuyun.com/midjourney/xxx.png",
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

**重要**：`taskId` 和 `imageId` 用于后续的 upsample、variation 等操作。

## 🚀 使用方式

### 方式1：直接生成并保存（推荐）⭐

```bash
curl -X POST http://localhost:8123/api/mj/generate \
  -H "Content-Type: application/json" \
  -H "Cookie: JSESSIONID=your-session-id" \
  -d '{
    "prompt": "The clothing pattern of the capybara Lulu"
  }'
```

**返回**：
```json
{
  "code": 0,
  "data": 1992847245649223683,
  "message": "ok"
}
```

### 方式2：先预览再决定是否保存

**步骤1**：预览生成效果
```bash
curl -X POST http://localhost:8123/api/mj/imagine \
  -H "Content-Type: application/json" \
  -d '{
    "prompt": "The clothing pattern of the capybara Lulu"
  }'
```

**步骤2**：如果满意，再调用保存接口
```bash
curl -X POST http://localhost:8123/api/mj/generate \
  -H "Content-Type: application/json" \
  -H "Cookie: JSESSIONID=your-session-id" \
  -d '{
    "prompt": "The clothing pattern of the capybara Lulu"
  }'
```

## 📊 查询 MJ 图案

### 通过现有接口查询

使用 `/api/pattern/list` 接口，添加 `generationType` 过滤：

```bash
curl -X POST http://localhost:8123/api/pattern/list \
  -H "Content-Type: application/json" \
  -d '{
    "generationType": "MJ_GENERATED",
    "current": 1,
    "pageSize": 10
  }'
```

### SQL 直接查询

```sql
-- 查询所有 MJ 图案
SELECT * FROM pattern 
WHERE generationType = 'MJ_GENERATED' 
ORDER BY createTime DESC;

-- 查询某用户的 MJ 图案
SELECT * FROM pattern 
WHERE userId = 1 AND generationType = 'MJ_GENERATED';

-- 提取 MJ 信息
SELECT 
  id,
  patternName,
  JSON_EXTRACT(generationParams, '$.taskId') as taskId,
  JSON_EXTRACT(generationParams, '$.imageId') as imageId,
  JSON_EXTRACT(generationParams, '$.rawImageUrl') as imageUrl
FROM pattern 
WHERE generationType = 'MJ_GENERATED';
```

## 🔄 后续操作流程

### Upsample（放大）某一张图

**步骤1**：获取图案信息
```java
Pattern pattern = patternService.getById(patternId);
MJImagineResponse mjInfo = JSON.parseObject(
    pattern.getGenerationParams(), 
    MJImagineResponse.class
);
```

**步骤2**：执行 Upsample
```bash
curl -X POST http://localhost:8123/api/mj/action \
  -H "Content-Type: application/json" \
  -d '{
    "taskId": "从步骤1获取",
    "imageId": "从步骤1获取",
    "action": "upsample1"
  }'
```

**步骤3**（可选）：将放大后的图片保存为新图案
```java
// 手动创建新的 Pattern 记录
Pattern newPattern = new Pattern();
newPattern.setUserId(userId);
newPattern.setPatternName("MJ-Upscaled-" + originalName);
newPattern.setDescription(originalPrompt + " (Upscaled)");
newPattern.setGenerationType("MJ_GENERATED");
newPattern.setPatternUrl(upsampledImageUrl);
// ... 其他字段
patternService.save(newPattern);
```

## 🎯 完整工作流程示例

### 场景：用户生成图案并进行优化

```
1. 用户登录系统
   ↓
2. 输入提示词："可爱的水豚图案，卡通风格"
   ↓
3. 调用 POST /api/mj/generate
   ↓
4. 系统生成图片并保存（返回图案ID: 123）
   ↓
5. 用户在图案详情页查看（4张候选图）
   ↓
6. 用户选择第2张图进行放大
   ↓
7. 前端从 generationParams 获取 taskId 和 imageId
   ↓
8. 调用 POST /api/mj/action (action: "upsample2")
   ↓
9. 获得高清大图
   ↓
10. 可选：保存为新图案或更新原图案
```

## 📋 数据库更新说明

**好消息**：根据你的建表语句，`generationType` 字段是 `VARCHAR(50)` 类型，**无需修改表结构**！

```sql
`generationType` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL
```

可以直接存储 `'MJ_GENERATED'` 值，无需执行任何 SQL 更新。

如果你想验证，可以执行：
```sql
-- 查看字段定义
SHOW FULL COLUMNS FROM `pattern` WHERE Field = 'generationType';
```

## ⚠️ 注意事项

1. **登录验证**
   - `/api/mj/generate` 接口需要用户登录
   - 使用 `userService.getLoginUser(httpRequest)` 获取当前用户

2. **审核流程**
   - 生成的图案默认为 `PENDING`（待审核）
   - 需要管理员审核后才能在前端展示
   - 可以在管理后台统一审核

3. **图片存储**
   - 当前直接使用 MJ 提供的 CDN URL
   - 如需保存到自己的 COS，需要额外开发下载上传功能

4. **命名规则**
   - 自动截取 prompt 前30字符作为图案名称
   - 格式：`"MJ-" + prompt.substring(0, 30)`
   - 用户可以后续修改

5. **JSON 存储**
   - `generationParams` 字段存储完整的 MJ 响应
   - 包含 taskId、imageId 等重要信息
   - 用于后续的 upsample、variation 操作

## 🔧 前端集成建议

### 1. 图案生成页面

```vue
<template>
  <div>
    <el-input v-model="prompt" placeholder="输入图案描述" />
    <el-button @click="generate" :loading="loading">生成图案</el-button>
  </div>
</template>

<script setup>
import { ref } from 'vue';
import { useRouter } from 'vue-router';

const router = useRouter();
const prompt = ref('');
const loading = ref(false);

async function generate() {
  loading.value = true;
  try {
    const res = await fetch('/api/mj/generate', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ prompt: prompt.value })
    });
    const result = await res.json();
    
    if (result.code === 0) {
      // 跳转到图案详情页
      router.push(`/pattern/${result.data}`);
    }
  } finally {
    loading.value = false;
  }
}
</script>
```

### 2. 图案详情页（显示操作按钮）

```vue
<template>
  <div>
    <img :src="pattern.patternUrl" />
    
    <!-- 如果是 MJ 生成的，显示操作按钮 -->
    <div v-if="pattern.generationType === 'MJ_GENERATED'">
      <el-button @click="upsample(1)">放大图1</el-button>
      <el-button @click="upsample(2)">放大图2</el-button>
      <el-button @click="upsample(3)">放大图3</el-button>
      <el-button @click="upsample(4)">放大图4</el-button>
      
      <el-button @click="variation(1)">变体1</el-button>
      <el-button @click="variation(2)">变体2</el-button>
      <el-button @click="variation(3)">变体3</el-button>
      <el-button @click="variation(4)">变体4</el-button>
      
      <el-button @click="reroll">重新生成</el-button>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue';

const pattern = ref({});

async function upsample(index) {
  const mjInfo = JSON.parse(pattern.value.generationParams);
  
  const res = await fetch('/api/mj/action', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({
      taskId: mjInfo.taskId,
      imageId: mjInfo.imageId,
      action: `upsample${index}`
    })
  });
  
  const result = await res.json();
  // 处理结果...
}
</script>
```

## 📚 相关文档

- `Midjourney接口使用说明.md` - API 详细文档
- `Midjourney快速开始.md` - 快速入门
- `Midjourney保存图案使用指南.md` - 完整使用指南
- `sql/update_add_mj_generation_type.sql` - 数据库说明

## ✅ 功能清单

- [x] 添加 MJ_GENERATED 枚举类型
- [x] 实现生成并保存接口
- [x] 保存到 pattern 表
- [x] 存储完整的 MJ 响应信息
- [x] 支持用户登录验证
- [x] 自动设置审核状态
- [x] 提供测试用例
- [x] 编写完整文档

## 🎉 总结

通过复用现有的 `pattern` 表，我们实现了：

1. ✅ **统一管理**：所有图案在一个表中
2. ✅ **功能复用**：审核、点赞、收藏等功能直接可用
3. ✅ **扩展性好**：未来接入其他 AI 服务也可以使用同一套结构
4. ✅ **无需改表**：VARCHAR 类型字段直接支持新值
5. ✅ **完整信息**：generationParams 保存所有 MJ 响应数据

**现在就可以开始使用了！** 🚀

---

**更新时间**：2025-11-28  
**版本**：1.0

