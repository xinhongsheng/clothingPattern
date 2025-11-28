# Midjourney 图案生成功能说明文档

## 功能概述

本系统集成了 Midjourney API，实现了专业的服装图案智能创作功能。用户可以通过文字描述生成高质量的服装图案，并进行多次迭代优化。

## 核心功能

### 1. Imagine（图案生成）

**接口**: `POST /api/mj/imagine`

**功能**: 根据用户输入的提示词生成 2x2 网格的候选图案（4张图片）

**请求参数**:
```json
{
  "prompt": "可爱的小猫图案",
  "action": "generate",
  "style": "卡通",
  "season": "四季",
  "targetAudience": "儿童"
}
```

**响应数据**:
```json
{
  "code": 0,
  "data": {
    "taskId": "xxx",
    "imageId": "yyy",
    "imageUrl": "缩略图URL",
    "rawImageUrl": "原始图片URL",
    "subImageUrls": ["子图1", "子图2", "子图3", "子图4"],
    "actions": ["upsample1", "upsample2", ...],
    "success": true
  }
}
```

**特性**:
- 自动中文翻译：系统会自动将中文提示词翻译成英文
- 专业术语优化：自动添加服装行业专业前缀
- 风格标签：支持简约、可爱、复古、卡通等多种风格
- 季节适配：支持春夏秋冬及四季通用
- 受众定位：支持儿童、青少年、成人等目标受众

### 2. Action（图案操作）

**接口**: `POST /api/mj/action`

**功能**: 对生成的图案执行二次操作

**支持的操作类型**:

#### 2.1 Upsample（放大）
- `upsample1`: 放大第1张图片（左上）
- `upsample2`: 放大第2张图片（右上）
- `upsample3`: 放大第3张图片（左下）
- `upsample4`: 放大第4张图片（右下）

**用途**: 将选中的图片放大为高清大图，适合最终使用

#### 2.2 Variation（变体）
- `variation1`: 生成第1张图片的变体
- `variation2`: 生成第2张图片的变体
- `variation3`: 生成第3张图片的变体
- `variation4`: 生成第4张图片的变体

**用途**: 基于选中的图片生成相似风格的新图案（又是4张）

#### 2.3 Reroll（重新生成）
- `reroll`: 使用相同的提示词重新生成全新的4张图片

**用途**: 对当前结果不满意时，重新生成

**请求参数**:
```json
{
  "taskId": "从imagine响应中获取",
  "imageId": "从imagine响应中获取",
  "action": "upsample1"
}
```

### 3. Blend（垫图/混合）

**接口**: `POST /api/mj/blend`

**功能**: 将2-5张图片混合生成新的图案

**请求参数**:
```json
{
  "imageUrls": [
    "https://example.com/image1.png",
    "https://example.com/image2.png"
  ],
  "action": "blend",
  "style": "简约",
  "season": "春季",
  "targetAudience": "成人"
}
```

**用途**: 
- 结合多个参考图片的元素
- 创造独特的混合风格
- 实现图片到图片的转换

### 4. Save（保存图案）

**接口**: `POST /api/mj/save`

**功能**: 将生成的图案保存到数据库

**请求参数**:
```json
{
  "patternName": "我的图案名称",
  "prompt": "原始提示词",
  "rawImageUrl": "原始图片URL",
  "imageUrl": "缩略图URL",
  "taskId": "任务ID",
  "imageId": "图片ID",
  "style": "风格",
  "season": "季节",
  "targetAudience": "受众"
}
```

**数据库字段**:
- `patternName`: 图案名称
- `description`: 图案描述（原始提示词）
- `generationType`: MJ_GENERATED
- `patternUrl`: 图案URL
- `style`: 风格标签
- `season`: 季节标签
- `targetAudience`: 受众标签
- `generationParams`: MJ响应的完整JSON

## 完整工作流

### 用户操作流程

```
1. 输入提示词和风格信息
   ↓
2. 系统生成 2x2 网格（4张候选图）
   ↓
3. 用户选择喜欢的图片位置
   ↓
4. 执行操作：
   - 放大（Upsample）→ 获得高清大图 → 保存
   - 变体（Variation）→ 获得新的4张候选图 → 重复步骤3
   - 重新生成（Reroll）→ 获得全新的4张候选图 → 重复步骤3
   ↓
5. 满意后保存到数据库
   ↓
6. 可选：继续对保存的图片执行操作（迭代优化）
```

### 图片位置说明

2x2 网格布局：
```
+-------+-------+
|   1   |   2   |
| 左上  | 右上  |
+-------+-------+
|   3   |   4   |
| 左下  | 右下  |
+-------+-------+
```

## 技术实现

### 后端架构

```
Controller层 (MJController)
    ↓
Service层 (PromptTranslateService)
    ↓
SDK层 (MJGenImage)
    ↓
Midjourney API
```

### 核心类说明

#### 1. MJGenImage.java
- `imagine()`: 调用 Imagine API
- `executeAction()`: 调用 Action API
- `blend()`: 调用 Blend API

#### 2. PromptTranslateService.java
- `translateAndOptimize()`: 翻译并优化提示词
- 自动检测中文并翻译
- 添加服装行业专业前缀
- 组合风格、季节、受众信息

#### 3. MJController.java
- `/imagine`: 生成图案（不保存）
- `/generate`: 生成图案并保存
- `/action`: 执行操作
- `/blend`: 混合图片
- `/save`: 保存图案

### 前端实现

#### MJPatternGenerationPage.vue

**三步向导**:
1. **步骤1**: 输入提示词和风格信息
2. **步骤2**: 查看2x2网格，选择操作
3. **步骤3**: 确认并保存

**核心功能**:
- 表单验证
- 实时预览
- 操作按钮（Upsample/Variation/Reroll）
- 保存到数据库
- 继续操作（不保存）

## 配置说明

### application.yml

```yaml
mj:
  api:
    token: your_api_token_here
    url: https://api.zhishuyun.com/midjourney/imagine
```

### 超时设置

```java
// MJGenImage.java
connectTimeout: 60秒
readTimeout: 180秒（3分钟）
writeTimeout: 60秒
```

**原因**: Midjourney 生成图片通常需要 30-120 秒

## 使用示例

### 示例1：生成可爱的儿童图案

```json
POST /api/mj/imagine
{
  "prompt": "可爱的小兔子和胡萝卜",
  "style": "卡通",
  "season": "四季",
  "targetAudience": "儿童"
}
```

**系统处理**:
1. 检测到中文，调用通义千问翻译
2. 添加专业前缀："Professional clothing pattern design, fashion textile print, "
3. 组合结果："Professional clothing pattern design, fashion textile print, cute rabbit and carrot, cartoon style, for all seasons, target audience: children"
4. 调用 Midjourney API
5. 返回 2x2 网格图片

### 示例2：放大并保存

```json
// 步骤1：放大第1张图片
POST /api/mj/action
{
  "taskId": "task_xxx",
  "imageId": "img_yyy",
  "action": "upsample1"
}

// 步骤2：保存到数据库
POST /api/mj/save
{
  "patternName": "可爱小兔子图案-高清版",
  "prompt": "可爱的小兔子和胡萝卜",
  "rawImageUrl": "从action响应中获取",
  "imageUrl": "从action响应中获取",
  "style": "卡通",
  "season": "四季",
  "targetAudience": "儿童"
}
```

### 示例3：生成变体并继续优化

```json
// 步骤1：生成第2张图片的变体
POST /api/mj/action
{
  "taskId": "task_xxx",
  "imageId": "img_yyy",
  "action": "variation2"
}

// 步骤2：对变体结果再次执行upsample
POST /api/mj/action
{
  "taskId": "从variation响应中获取新的taskId",
  "imageId": "从variation响应中获取新的imageId",
  "action": "upsample3"
}

// 步骤3：保存最终结果
POST /api/mj/save
{...}
```

## 注意事项

### 1. 性能优化
- 生成时间：30-120秒
- 建议使用异步处理
- 前端显示加载动画

### 2. 错误处理
- 超时重试机制
- 详细的错误日志
- 用户友好的错误提示

### 3. 数据库存储
- `generationType`: 使用 `MJ_GENERATED`
- `generationParams`: 存储完整的 MJ 响应 JSON
- `style`, `season`, `targetAudience`: 独立字段便于筛选

### 4. 安全性
- API Token 配置在服务器端
- 用户权限验证
- 管理员自动审核通过

## 扩展功能

### 未来可以添加的功能

1. **批量生成**: 一次生成多组图案
2. **历史记录**: 保存用户的生成历史
3. **收藏夹**: 收藏喜欢的图案
4. **分享功能**: 分享图案给其他用户
5. **AI推荐**: 根据用户喜好推荐风格
6. **图案编辑**: 在线编辑生成的图案
7. **导出功能**: 导出为不同格式（PNG, SVG, PDF）
8. **色彩调整**: 调整图案的色彩方案

## 常见问题

### Q1: 为什么生成时间这么长？
A: Midjourney 需要在服务器端进行复杂的 AI 计算，通常需要 30-120 秒。这是正常现象。

### Q2: 如何提高生成质量？
A: 
- 使用详细的描述
- 选择合适的风格标签
- 多次迭代优化（使用 Variation）
- 参考优秀案例

### Q3: Blend 功能如何使用？
A: Blend 需要提供 2-5 张图片的 URL，系统会将这些图片的元素混合生成新图案。

### Q4: 可以无限次操作吗？
A: 理论上可以，但每次操作都会消耗 API 配额。建议在满意后及时保存。

## 技术支持

如有问题，请联系技术团队或查看 Midjourney API 官方文档。

