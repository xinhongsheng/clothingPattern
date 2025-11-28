# Midjourney UI 交互功能实现说明

## 🎯 功能概述

实现了完整的 Midjourney 图案生成交互流程，用户可以：
1. 输入提示词生成 2x2 网格图片（4张候选图）
2. 选择喜欢的图片执行二次操作（upsample/variation/reroll）
3. 确认后保存到数据库

## ✨ 实现的功能

### 1. 三步式交互流程

#### 步骤1：输入提示词并生成
- 用户输入图案描述（支持中文）
- 系统自动翻译并添加服装专业前缀
- 生成 2x2 网格图片（4张候选图）

#### 步骤2：选择图片并执行操作
- 显示生成的 2x2 网格图片
- 提供三类操作：
  - **Upsample（放大）**：选择 1-4 中的一张生成高清大图
  - **Variation（变体）**：基于选中的图片生成相似风格的新图案
  - **Reroll（重新生成）**：生成全新的 4 张图案
- 执行操作后进入步骤3

#### 步骤3：确认并保存
- 显示最终生成的图片
- 用户输入图案名称
- 三个选项：
  - **返回重新选择**：回到步骤2
  - **继续操作（不保存）**：对当前图片继续执行操作
  - **确认保存**：保存到数据库并跳转到我的作品

### 2. 后端接口

#### 新增接口

**POST /api/mj/save** - 保存图片到数据库

**请求参数**：
```json
{
  "imageUrl": "缩略图URL",
  "rawImageUrl": "原始图片URL",
  "taskId": "任务ID",
  "imageId": "图片ID",
  "patternName": "用户输入的图案名称",
  "prompt": "原始提示词",
  ...其他MJ响应字段
}
```

**响应**：
```json
{
  "code": 0,
  "data": 123456,  // 保存的图案ID
  "message": "ok"
}
```

#### 已有接口

- `POST /api/mj/imagine` - 生成图片（不保存）
- `POST /api/mj/action` - 执行操作
- `POST /api/mj/generate` - 生成并直接保存（原有功能）

### 3. 前端页面

**文件**：`MJPatternGenerationPage.vue`

**路由**：`/mj/generation`

**功能特点**：
- ✅ 三步式交互流程
- ✅ 实时状态提示
- ✅ 操作按钮高亮选中
- ✅ 支持返回上一步
- ✅ 支持继续操作（不保存）
- ✅ 响应式设计（支持移动端）

## 🔄 完整工作流程

### 场景1：生成并保存

```
用户访问 /mj/generation
    ↓
步骤1：输入"可爱的小猫图案"
    ↓
系统翻译并生成 2x2 网格图片
    ↓
步骤2：用户选择"放大图片2"
    ↓
系统执行 upsample2 操作
    ↓
步骤3：用户输入图案名称"小猫图案-001"
    ↓
点击"确认保存"
    ↓
保存到数据库（状态：待审核）
    ↓
跳转到"我的创意"页面
```

### 场景2：多次操作后保存

```
用户访问 /mj/generation
    ↓
步骤1：输入"复古花卉图案"
    ↓
生成 2x2 网格图片
    ↓
步骤2：选择"变体图片3"
    ↓
执行 variation3
    ↓
步骤3：点击"继续操作（不保存）"
    ↓
回到步骤2（当前图片作为新起点）
    ↓
选择"放大图片1"
    ↓
执行 upsample1
    ↓
步骤3：输入图案名称并保存
    ↓
保存到数据库
```

### 场景3：不满意重新生成

```
用户访问 /mj/generation
    ↓
步骤1：输入"几何图案"
    ↓
生成 2x2 网格图片
    ↓
步骤2：用户不满意，点击"返回重新生成"
    ↓
回到步骤1
    ↓
修改提示词为"简约几何图案"
    ↓
重新生成
```

## 📊 数据流转

### 步骤1 → 步骤2

```javascript
// 调用 imagine 接口
const res = await imagine({
  prompt: "可爱的小猫图案",
  action: "generate"
})

// 保存响应数据
mjResponse.value = res.data.data
// {
//   imageUrl: "缩略图URL",
//   rawImageUrl: "原始图URL",
//   taskId: "xxx",
//   imageId: "xxx",
//   actions: ["upsample1", "upsample2", ...],
//   ...
// }
```

### 步骤2 → 步骤3

```javascript
// 调用 action 接口
const res = await mjExecuteAction({
  taskId: mjResponse.value.taskId,
  imageId: mjResponse.value.imageId,
  action: "upsample2"
})

// 保存最终结果
finalResult.value = res.data.data
// {
//   imageUrl: "新的缩略图URL",
//   rawImageUrl: "新的原始图URL",
//   taskId: "新的taskId",
//   imageId: "新的imageId",
//   ...
// }
```

### 步骤3 → 保存

```javascript
// 调用 save 接口
const res = await savePattern({
  ...finalResult.value,
  patternName: "用户输入的名称",
  prompt: "原始提示词"
})

// 返回图案ID
const patternId = res.data.data
```

## 🎨 UI 设计

### 步骤指示器

```
步骤1: 📝 描述创意
步骤2: 🖼️ 选择图案
步骤3: ✅ 确认保存
```

### 操作按钮分组

```
🔍 放大（Upsample）
  [放大图片1] [放大图片2] [放大图片3] [放大图片4]

🎨 变体（Variation）
  [变体图片1] [变体图片2] [变体图片3] [变体图片4]

🔄 重新生成（Reroll）
  [重新生成]
```

### 底部操作栏

```
步骤2: [返回重新生成]              [执行操作]
步骤3: [返回重新选择] [继续操作] [确认保存]
```

## 💡 技术实现

### 状态管理

```typescript
// 当前步骤
const currentStep = ref(1)  // 1, 2, 3

// 数据状态
const mjResponse = ref<any>(null)      // 步骤1的结果
const finalResult = ref<any>(null)     // 步骤2的结果
const selectedAction = ref<string | null>(null)  // 选中的操作

// 用户输入
const originalPrompt = ref('')  // 原始提示词
const saveForm = reactive({
  patternName: ''  // 图案名称
})
```

### 步骤切换逻辑

```typescript
// 步骤1 → 步骤2
const handleGenerate = async () => {
  const res = await imagine(...)
  mjResponse.value = res.data.data
  currentStep.value = 2
}

// 步骤2 → 步骤3
const executeAction = async () => {
  const res = await mjExecuteAction(...)
  finalResult.value = res.data.data
  currentStep.value = 3
}

// 步骤3 → 保存
const saveToDatabase = async () => {
  const res = await savePattern(...)
  router.push('/my-idea')
}

// 返回上一步
const backToStep1 = () => { currentStep.value = 1 }
const backToStep2 = () => { currentStep.value = 2 }

// 继续操作（不保存）
const continueWithoutSaving = () => {
  mjResponse.value = finalResult.value
  currentStep.value = 2
}
```

## 📝 后端实现

### MJController 新增方法

```java
@PostMapping("/save")
@Operation(summary = "保存图片到数据库")
public BaseResponse<Long> savePattern(@RequestBody MJImagineResponse request,
                                       HttpServletRequest httpRequest) {
    // 1. 获取登录用户
    User loginUser = userService.getLoginUser(httpRequest);
    
    // 2. 校验URL
    String rawImageUrl = request.getRawImageUrl();
    ThrowUtils.throwIf(StringUtils.isBlank(rawImageUrl), 
        ErrorCode.PARAMS_ERROR, "图片URL为空");
    
    // 3. 创建Pattern对象
    Pattern pattern = new Pattern();
    pattern.setUserId(loginUser.getId());
    pattern.setPatternName(request.getPatternName());
    pattern.setDescription(request.getPrompt());
    pattern.setGenerationType("MJ_GENERATED");
    pattern.setPatternUrl(rawImageUrl);
    pattern.setThumbUrl(request.getImageUrl());
    pattern.setAuditStatus("PENDING");
    pattern.setGenerationParams(JSON.toJSONString(request));
    
    // 4. 保存到数据库
    patternService.save(pattern);
    
    return ResultUtils.success(pattern.getId());
}
```

### MJImagineResponse 新增字段

```java
public class MJImagineResponse {
    // ... 原有字段
    
    /**
     * 图案名称（前端传入，用于保存）
     */
    private String patternName;
    
    /**
     * 提示词（前端传入，用于保存）
     */
    private String prompt;
}
```

## 🧪 测试场景

### 测试1：完整流程

1. 访问 `/mj/generation`
2. 输入"可爱的水豚图案"
3. 等待生成（约1-2分钟）
4. 选择"放大图片2"
5. 等待执行（约1-2分钟）
6. 输入图案名称"水豚-001"
7. 点击"确认保存"
8. 验证跳转到"我的创意"
9. 验证图案已保存

### 测试2：多次操作

1. 生成初始图片
2. 执行"变体图片3"
3. 点击"继续操作"
4. 执行"放大图片1"
5. 保存最终结果

### 测试3：中途返回

1. 生成初始图片
2. 选择操作但不满意
3. 点击"返回重新生成"
4. 修改提示词
5. 重新生成

## ⚠️ 注意事项

1. **生成时间**
   - 每次生成需要 1-2 分钟
   - 需要显示明确的等待提示
   - 建议显示进度或动画

2. **用户体验**
   - 操作按钮需要明确的选中状态
   - 提供返回上一步的功能
   - 支持继续操作（不保存）

3. **数据保存**
   - 只在用户点击"确认保存"时才保存
   - 保存时需要用户输入图案名称
   - 保存完整的 MJ 响应信息到 generationParams

4. **错误处理**
   - 网络超时的友好提示
   - 生成失败的重试机制
   - 保存失败的错误提示

## 📚 相关文档

- `Midjourney快速开始.md` - 快速入门
- `Midjourney接口使用说明.md` - API 文档
- `Midjourney服装图案专业化功能说明.md` - 翻译功能
- `Midjourney超时问题解决方案.md` - 超时问题

## ✅ 功能清单

- [x] 创建 MJPatternGenerationPage 前端页面
- [x] 实现三步式交互流程
- [x] 添加 /api/mj/save 后端接口
- [x] 更新 MJImagineResponse 添加字段
- [x] 添加路由配置
- [x] 实现操作选择UI
- [x] 实现继续操作功能
- [x] 实现保存到数据库
- [x] 添加响应式设计
- [x] 编写完整文档

## 🎉 总结

通过实现三步式交互流程，用户现在可以：

1. ✅ **灵活选择**：从 4 张候选图中选择喜欢的
2. ✅ **多次操作**：可以对图片进行多次优化
3. ✅ **按需保存**：只保存满意的最终结果
4. ✅ **完整记录**：保存所有操作历史到数据库

**现在可以开始测试了！** 🚀

---

**更新时间**：2025-11-28  
**版本**：1.0

