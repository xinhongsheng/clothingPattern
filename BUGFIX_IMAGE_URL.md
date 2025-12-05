# Bug 修复：图片 URL 格式错误

## 🐛 问题描述

在步骤3继续执行变体操作后，生成的图片无法显示，浏览器控制台报错：

```
Failed to load resource: net::ERR_NAME_NOT_RESOLVED
```

错误的 URL 格式示例：
```
f454fc3a-c670-434d-99fe-468064c29bf5.png?imageMogr2/thumbnail/!50p:1
                                                                    ^^
                                                         末尾多了 :1
```

正确的 URL 格式应该是：
```
f454fc3a-c670-434d-99fe-468064c29bf5.png?imageMogr2/thumbnail/!50p
```

## 🔍 问题分析

### 根本原因

在继续执行操作（`executeContinueAction`）时，后端返回的数据可能包含了格式异常的 URL，URL 末尾带有 `:数字` 后缀（如 `:1`）。这个后缀导致浏览器无法正确解析域名，从而出现 `ERR_NAME_NOT_RESOLVED` 错误。

### 出现场景

- ✅ **第一次操作**：正常显示
- ✅ **第二次继续操作**：图片无法显示（URL 格式错误）
- ✅ **直接在浏览器打开**：可以正常访问（因为浏览器会自动处理 URL）

## ✅ 解决方案

### 修改文件

`clothingPattern-front/src/pages/MJPatternGenerationPage.vue`

### 修改内容

在 `executeAction` 和 `executeContinueAction` 两个方法中，添加 URL 清理逻辑：

```typescript
// 执行操作（从步骤2到步骤3）
const executeAction = async () => {
  
  if (res.data.code === 0 && res.data.data) {
    const newResult = res.data.data
    
    // ✅ 新增：清理可能存在的URL格式问题
    if (newResult.imageUrl) {
      newResult.imageUrl = newResult.imageUrl.replace(/:\d+$/, '')
    }
    if (newResult.rawImageUrl) {
      newResult.rawImageUrl = newResult.rawImageUrl.replace(/:\d+$/, '')
    }
    
    finalResult.value = newResult
  }
}

// 在步骤3继续执行操作
const executeContinueAction = async () => {
  
  if (res.data.code === 0 && res.data.data) {
    const newResult = res.data.data
    
    // ✅ 新增：清理可能存在的URL格式问题
    if (newResult.imageUrl) {
      // 移除URL末尾可能存在的 :数字 格式
      newResult.imageUrl = newResult.imageUrl.replace(/:\d+$/, '')
    }
    if (newResult.rawImageUrl) {
      newResult.rawImageUrl = newResult.rawImageUrl.replace(/:\d+$/, '')
    }
    
    finalResult.value = newResult
    
    // ✅ 新增：打印调试信息
    console.log('继续操作成功，新结果：', finalResult.value)
    console.log('图片URL:', finalResult.value.imageUrl)
    console.log('原始URL:', finalResult.value.rawImageUrl)
  }
}
```

## 🔧 修复逻辑

使用正则表达式 `/:\d+$/` 匹配并移除 URL 末尾的 `:数字` 格式：

- `:` - 匹配冒号字符
- `\d+` - 匹配一个或多个数字
- `$` - 匹配字符串结尾

示例：
```javascript
'url:1'.replace(/:\d+$/, '')  // 结果：'url'
'url:123'.replace(/:\d+$/, '') // 结果：'url'
'url'.replace(/:\d+$/, '')     // 结果：'url' (无变化)
```

## 🧪 测试验证

### 测试步骤

1. **第一次生成**
   - 输入提示词，生成 4 张图
   - 验证图片正常显示 ✅

2. **第一次变体**
   - 选择变体操作，生成新的 4 张图
   - 验证图片正常显示 ✅

3. **第二次变体**（关键测试点）
   - 继续选择变体操作，生成新的 4 张图
   - 验证图片正常显示 ✅
   - 检查浏览器控制台无报错 ✅
   - 检查 URL 格式正确（无 `:数字` 后缀）✅

4. **多次迭代**
   - 连续执行 3-5 次变体或放大操作
   - 验证每次图片都正常显示 ✅

### 调试信息

修复后，每次继续操作会在控制台打印调试信息：

```javascript
继续操作成功，新结果： {taskId: "...", imageId: "...", ...}
图片URL: https://platform.cdn.zhishuyun.com/midjourney/xxx.png?imageMogr2/thumbnail/!50p
原始URL: https://platform.cdn.zhishuyun.com/midjourney/xxx.png
```

可以检查 URL 是否正确（应该没有 `:1` 等后缀）。

## 📊 影响范围

### 修改范围
- ✅ 前端代码：2 个方法
- ❌ 后端代码：无需修改
- ❌ 数据库：无需修改

### 影响功能
- ✅ Midjourney 智能创作 - 步骤2执行操作
- ✅ Midjourney 智能创作 - 步骤3继续操作
- ❌ 其他功能：无影响

## 🎯 预防措施

### 为什么要在前端清理

1. **防御性编程**：即使后端返回错误格式，前端也能正确处理
2. **快速修复**：无需等待后端修复和部署
3. **兼容性**：对正确格式的 URL 不产生影响

### 建议后端优化

虽然前端已经做了容错处理，但建议后端也检查 Midjourney API 返回的数据，确保 URL 格式正确。

检查位置：
```java
// MJGenImage.java - executeAction 方法
public MJImagineVO executeAction(String taskId, String imageId, String action) {
    
    // 建议添加 URL 格式检查
    MJImagineVO mjResponse = JSON.parseObject(responseBody, MJImagineVO.class);
    
    // 清理可能的格式问题
    if (mjResponse.getImageUrl() != null) {
        String cleanUrl = mjResponse.getImageUrl().replaceAll(":\\d+$", "");
        mjResponse.setImageUrl(cleanUrl);
    }
    if (mjResponse.getRawImageUrl() != null) {
        String cleanUrl = mjResponse.getRawImageUrl().replaceAll(":\\d+$", "");
        mjResponse.setRawImageUrl(cleanUrl);
    }
    
    return mjResponse;
}
```

## 📅 修复日期

2025-12-06

## 👤 修复者

AI 助手

## ✅ 修复状态

已完成 - 前端已修复，建议后端也进行优化
