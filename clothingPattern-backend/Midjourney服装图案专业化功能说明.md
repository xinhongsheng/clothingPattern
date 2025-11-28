# Midjourney 服装图案专业化功能说明

## 🎯 功能概述

为 Midjourney 图片生成服务添加了两个重要的专业化功能：

1. **自动翻译中文提示词** - 将用户输入的中文描述翻译成专业的英文提示词
2. **添加服装行业前缀** - 自动为所有提示词添加服装图案设计的专业前缀

## ✨ 新增功能

### 1. Prompt 翻译服务

**文件**: `PromptTranslateService.java`

**功能**:
- ✅ 自动检测输入语言（中文/英文）
- ✅ 使用通义千问 AI 翻译中文到英文
- ✅ 优化翻译为服装设计专业术语
- ✅ 自动添加服装图案专业前缀
- ✅ 翻译失败时的容错处理

### 2. 服装专业前缀

**前缀模板**:
```
Professional clothing pattern design, fashion textile print,
```

**作用**:
- 明确告诉 AI 生成服装图案
- 提高生成图片的专业性
- 确保输出符合服装行业需求

## 🔧 实现细节

### PromptTranslateService 类

```java
@Service
public class PromptTranslateService {
    
    // 服装专业前缀
    private static final String CLOTHING_PATTERN_PREFIX = 
        "Professional clothing pattern design, fashion textile print, ";
    
    // 主要方法
    public String translateAndOptimize(String userPrompt) {
        // 1. 检测是否包含中文
        // 2. 如果是中文，调用 AI 翻译
        // 3. 添加服装专业前缀
        // 4. 返回优化后的提示词
    }
}
```

### 翻译系统提示词

```
你是一名专业的服装图案设计翻译专家。你的任务是：
1. 将用户输入的中文描述翻译成专业的英文服装图案设计提示词
2. 如果输入已经是英文，则直接优化为专业的服装图案描述
3. 翻译时要考虑服装设计的专业术语和行业规范
4. 输出应该简洁、专业、适合用于 AI 图案生成
5. 只返回翻译后的英文提示词，不要有任何解释或额外内容
```

### MJController 修改

```java
@PostMapping("/generate")
public BaseResponse<Long> generateAndSave(...) {
    String originalPrompt = request.getPrompt();  // 用户输入
    
    // 翻译并优化
    String optimizedPrompt = promptTranslateService.translateAndOptimize(originalPrompt);
    
    // 使用优化后的 prompt 调用 MJ API
    request.setPrompt(optimizedPrompt);
    MJImagineResponse mjResponse = mjGenImage.imagine(request);
    
    // 保存时使用原始 prompt（用户输入的）
    pattern.setDescription(originalPrompt);
}
```

## 📊 工作流程

### 用户输入中文

```
用户输入: "可爱的水豚图案"
    ↓
检测语言: 包含中文
    ↓
调用 AI 翻译: "cute capybara pattern"
    ↓
添加专业前缀: "Professional clothing pattern design, fashion textile print, cute capybara pattern"
    ↓
调用 MJ API 生成图片
    ↓
保存到数据库:
  - description: "可爱的水豚图案" (原始输入)
  - generationParams: 包含完整的 MJ 响应
```

### 用户输入英文

```
用户输入: "floral pattern"
    ↓
检测语言: 英文
    ↓
直接添加前缀: "Professional clothing pattern design, fashion textile print, floral pattern"
    ↓
调用 MJ API 生成图片
    ↓
保存到数据库:
  - description: "floral pattern" (原始输入)
```

## 🎨 翻译示例

| 用户输入（中文） | AI 翻译（英文） | 最终 Prompt |
|----------------|----------------|-------------|
| 可爱的水豚图案 | cute capybara pattern | Professional clothing pattern design, fashion textile print, cute capybara pattern |
| 复古花卉图案，适合夏季连衣裙 | vintage floral pattern for summer dress | Professional clothing pattern design, fashion textile print, vintage floral pattern for summer dress |
| 简约几何线条，黑白配色 | minimalist geometric lines, black and white color scheme | Professional clothing pattern design, fashion textile print, minimalist geometric lines, black and white color scheme |
| 卡通动物印花，儿童T恤 | cartoon animal print for children's t-shirt | Professional clothing pattern design, fashion textile print, cartoon animal print for children's t-shirt |

## 💡 技术特点

### 1. 智能语言检测

```java
private boolean containsChinese(String text) {
    // 使用正则表达式检测中文字符
    return text.matches(".*[\\u4e00-\\u9fa5]+.*");
}
```

### 2. AI 驱动翻译

- 使用通义千问 `qwen-plus` 模型
- 温度设置为 0.3（更稳定的翻译）
- 限制输出长度为 200 tokens
- 专业的系统提示词

### 3. 容错处理

```java
try {
    // 翻译逻辑
} catch (Exception e) {
    log.error("翻译提示词失败，使用原始输入", e);
    // 至少添加前缀
    return CLOTHING_PATTERN_PREFIX + userPrompt;
}
```

### 4. 结果清理

```java
// 去除可能的引号、换行等
translatedText = translatedText.trim()
    .replaceAll("^[\"']|[\"']$", "")  // 去除首尾引号
    .replaceAll("\\n", " ")            // 替换换行为空格
    .replaceAll("\\s+", " ");          // 合并多个空格
```

## 📝 日志输出

### 成功翻译

```
原始提示词：可爱的水豚图案
检测到中文提示词，开始翻译：可爱的水豚图案
翻译结果：cute capybara pattern
最终提示词：Professional clothing pattern design, fashion textile print, cute capybara pattern
调用Midjourney API，请求参数：{"action":"generate","prompt":"Professional clothing pattern design, fashion textile print, cute capybara pattern"}
```

### 英文输入

```
原始提示词：floral pattern
检测到英文提示词，直接添加专业前缀：floral pattern
最终提示词：Professional clothing pattern design, fashion textile print, floral pattern
调用Midjourney API，请求参数：{"action":"generate","prompt":"Professional clothing pattern design, fashion textile print, floral pattern"}
```

## 🧪 测试用例

### 测试1：中文输入

```bash
curl -X POST http://localhost:8123/api/mj/generate \
  -H "Content-Type: application/json" \
  -H "Cookie: JSESSIONID=xxx" \
  -d '{
    "prompt": "可爱的小猫图案，适合童装"
  }'
```

**预期**:
- 自动翻译成英文
- 添加服装专业前缀
- 生成服装图案风格的图片
- 数据库保存原始中文描述

### 测试2：英文输入

```bash
curl -X POST http://localhost:8123/api/mj/generate \
  -H "Content-Type: application/json" \
  -H "Cookie: JSESSIONID=xxx" \
  -d '{
    "prompt": "geometric pattern, modern style"
  }'
```

**预期**:
- 直接添加服装专业前缀
- 生成服装图案风格的图片
- 数据库保存原始英文描述

### 测试3：中英混合

```bash
curl -X POST http://localhost:8123/api/mj/generate \
  -H "Content-Type: application/json" \
  -H "Cookie: JSESSIONID=xxx" \
  -d '{
    "prompt": "复古 vintage 花卉图案"
  }'
```

**预期**:
- 检测到中文，进行翻译
- 优化整个提示词
- 添加服装专业前缀

## 📊 数据库存储

### Pattern 表字段

| 字段 | 值 | 说明 |
|------|-----|------|
| `description` | 用户原始输入 | "可爱的水豚图案" |
| `generationType` | "MJ_GENERATED" | 生成类型 |
| `generationParams` | 完整 MJ 响应 JSON | 包含实际使用的 prompt |

### generationParams 示例

```json
{
  "prompt": "Professional clothing pattern design, fashion textile print, cute capybara pattern",
  "imageUrl": "https://...",
  "rawImageUrl": "https://...",
  "taskId": "xxx",
  "imageId": "xxx",
  ...
}
```

**注意**: 实际使用的完整 prompt（包含前缀和翻译）会保存在 `generationParams` 中，方便后续查看和复用。

## ⚙️ 配置说明

### 依赖的配置

```yaml
# application.yml

# 通义千问 API Key（用于翻译）
tong-yi:
  config:
    dashscope-api-key: sk-xxx

# Midjourney API
mj:
  api:
    token: xxx
    url: https://api.zhishuyun.com/midjourney/imagine
```

## 🎯 优势

### 1. 用户体验优化

- ✅ 用户可以用中文输入，无需学习英文提示词
- ✅ 自动优化为专业术语
- ✅ 保留原始输入，方便查看

### 2. 生成质量提升

- ✅ 专业前缀引导 AI 生成服装图案
- ✅ 翻译优化确保术语准确
- ✅ 输出更符合服装行业需求

### 3. 系统专业化

- ✅ 明确定位为服装图案系统
- ✅ 所有生成都带有行业特征
- ✅ 便于后续扩展（如添加更多行业术语）

## 🔄 后续优化建议

### 短期优化

1. **提示词模板库**
   - 预设常用服装图案类型
   - 用户可选择模板快速生成

2. **翻译缓存**
   - 缓存常用翻译结果
   - 减少 AI 调用次数

3. **多语言支持**
   - 支持更多语言输入
   - 日语、韩语等

### 中期优化

1. **智能提示词优化**
   - 分析用户输入意图
   - 自动添加风格、色彩等参数

2. **提示词历史**
   - 记录用户常用提示词
   - 提供智能推荐

3. **A/B 测试**
   - 对比有无前缀的生成效果
   - 优化前缀内容

### 长期优化

1. **专业词库**
   - 建立服装设计专业词库
   - 提高翻译准确性

2. **风格迁移**
   - 根据历史生成学习用户偏好
   - 自动调整提示词风格

3. **多模态输入**
   - 支持参考图片 + 文字描述
   - 更精准的图案生成

## 📚 相关文档

- `Midjourney快速开始.md` - 快速入门
- `Midjourney接口使用说明.md` - API 文档
- `Midjourney超时问题解决方案.md` - 超时问题
- `MidjourneyJSON字段映射问题修复.md` - JSON 映射

## ✅ 功能清单

- [x] 创建 PromptTranslateService 服务类
- [x] 实现中文检测功能
- [x] 集成通义千问翻译 API
- [x] 添加服装专业前缀
- [x] 修改 MJController 调用翻译服务
- [x] 保存原始用户输入
- [x] 添加详细日志
- [x] 容错处理
- [x] 编写完整文档

## 🎉 总结

通过添加自动翻译和专业前缀功能，系统现在能够：

1. ✅ **支持中文输入** - 用户体验更友好
2. ✅ **专业化输出** - 生成的图片更符合服装行业需求
3. ✅ **智能优化** - AI 驱动的提示词优化
4. ✅ **完整记录** - 保留原始输入和优化结果

**现在可以开始测试了！** 🚀

---

**更新时间**: 2025-11-28  
**版本**: 1.0

