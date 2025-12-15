# Design Document - HomePage Beautification

## Overview

本设计文档描述了 HomePage 页面美化优化的技术方案。通过改进视觉设计、优化交互体验和增强响应式布局，提升用户在首页的浏览体验。设计遵循现代 UI/UX 最佳实践，使用渐变、阴影、动画等视觉元素创造更具吸引力的界面。

## Architecture

### 组件结构

HomePage 组件保持现有的三层结构：
1. **Hero Section** - 顶部欢迎区域（标题、副标题、搜索框）
2. **Filter Section** - 筛选条件区域（风格、季节、目标受众标签）
3. **Content Section** - 内容展示区域（图案列表、分页）

### 样式架构

采用 Vue 3 的 Scoped CSS，使用 CSS 变量和深度选择器来定制 Ant Design Vue 组件样式。样式组织按照从上到下的视觉层次结构。

## Components and Interfaces

### Hero Section 优化

**视觉改进：**
- 使用更鲜艳的渐变背景（从 #667eea 到 #764ba2 改为 #6366f1 到 #8b5cf6）
- 增加背景装饰元素（几何图形或图案）
- 优化标题字体大小和间距
- 添加微妙的动画效果（淡入、上浮）

**搜索框改进：**
- 增大搜索框尺寸（高度从 56px 增加到 64px）
- 优化阴影效果，增强立体感
- 改进悬停和聚焦状态的视觉反馈
- 搜索按钮使用更鲜明的渐变色

### Filter Section 优化

**布局改进：**
- 增加卡片内边距，提供更宽松的呼吸感
- 优化标签间距，避免拥挤
- 添加分隔线或视觉分组

**标签样式改进：**
- 使用更圆润的圆角（从 20px 增加到 24px）
- 优化未选中状态的背景色和边框
- 选中状态使用更鲜艳的渐变背景
- 添加悬停时的阴影和位移效果
- 改进过渡动画的缓动函数

### Pagination 优化

**样式改进：**
- 增大分页按钮尺寸和间距
- 优化选中状态的视觉效果
- 添加悬停动画（颜色变化、轻微上浮）
- 改进禁用状态的视觉表现

### 响应式设计优化

**断点策略：**
- Desktop: > 1024px - 完整布局
- Tablet: 768px - 1024px - 中等间距
- Mobile: < 768px - 紧凑布局，垂直排列

**移动端特殊处理：**
- 筛选标签改为垂直布局
- 减小字体和元素尺寸
- 优化触摸目标大小（最小 44x44px）

## Data Models

无需修改数据模型，保持现有的：
- `API.PatternVO` - 图案数据
- `API.PatternQueryRequest` - 查询参数

## 
Correctness Properties

*A property is a characteristic or behavior that should hold true across all valid executions of a system-essentially, a formal statement about what the system should do. Properties serve as the bridge between human-readable specifications and machine-verifiable correctness guarantees.*

由于本功能主要涉及 UI 样式和视觉效果的改进，大部分验收标准属于主观的视觉评价（如"现代感"、"流畅"、"自然"），不适合通过自动化测试验证。但我们可以为一些具体的样式属性定义示例测试。

**Example 1: 搜索框尺寸验证**
验证搜索框元素的高度是否达到设计要求的 64px
**Validates: Requirements 2.1**

**Example 2: 标签圆角样式验证**
验证筛选标签元素是否具有 border-radius 样式属性
**Validates: Requirements 3.1**

**Example 3: 动画过渡时长验证**
验证可交互元素的 transition-duration 是否设置为 0.3s
**Validates: Requirements 6.2**

**Example 4: 移动端响应式布局验证**
验证在移动视口宽度（< 768px）下，筛选区域的布局方向是否为垂直（column）
**Validates: Requirements 4.3**

**Example 5: 分页按钮选中状态验证**
验证选中的分页按钮是否具有高亮背景色样式类
**Validates: Requirements 5.3**

注意：由于这是一个 UI 美化项目，主要关注视觉效果和用户体验，大部分改进需要通过人工视觉检查来验证。上述示例测试主要用于确保关键样式属性的存在，而不是验证视觉效果的质量。

## Error Handling

本功能主要涉及样式改进，不涉及新的错误处理逻辑。保持现有的错误处理机制：
- API 请求失败时显示错误消息
- 数据加载失败时显示友好提示

## Testing Strategy

### 单元测试

由于本功能主要是 CSS 样式改进，传统的单元测试价值有限。建议的测试方法：

1. **组件渲染测试**
   - 验证 HomePage 组件能够正常渲染
   - 验证关键 DOM 元素存在（Hero Section、Filter Section、搜索框等）

2. **样式快照测试**
   - 使用 Jest 快照测试捕获组件的渲染输出
   - 确保样式修改不会意外破坏现有结构

3. **响应式测试**
   - 使用不同视口尺寸测试组件渲染
   - 验证媒体查询是否正确应用

### 手动测试

UI 美化项目的主要验证方式：

1. **视觉回归测试**
   - 在不同浏览器中检查页面显示（Chrome、Firefox、Safari）
   - 在不同设备上测试响应式布局（桌面、平板、手机）
   - 验证颜色、间距、字体等视觉元素

2. **交互测试**
   - 测试悬停效果（搜索框、标签、分页按钮）
   - 测试点击反馈
   - 测试动画流畅度

3. **可访问性测试**
   - 验证颜色对比度符合 WCAG 标准
   - 测试键盘导航
   - 验证焦点状态清晰可见

### 测试工具

- **Vitest** - Vue 组件单元测试
- **@vue/test-utils** - Vue 组件测试工具
- **浏览器开发者工具** - 响应式设计测试
- **Lighthouse** - 性能和可访问性审计

### 测试覆盖目标

- 组件渲染测试：确保所有关键元素正常渲染
- 样式快照测试：防止意外的样式回归
- 手动视觉测试：验证美化效果达到设计要求

## Implementation Notes

### CSS 最佳实践

1. **使用 CSS 变量**
   - 定义主题颜色、间距等可复用变量
   - 便于后续主题定制

2. **性能优化**
   - 使用 `transform` 和 `opacity` 实现动画（GPU 加速）
   - 避免使用 `width`、`height` 等触发重排的属性做动画

3. **浏览器兼容性**
   - 使用 autoprefixer 自动添加浏览器前缀
   - 测试主流浏览器的兼容性

4. **可维护性**
   - 保持样式代码的组织性和可读性
   - 添加注释说明复杂的样式规则

### 渐进增强

- 确保基础功能在不支持高级 CSS 特性的浏览器中仍可用
- 动画和过渡效果作为增强体验，不影响核心功能

## Dependencies

- Vue 3
- Ant Design Vue
- 现有的 PatternList 组件
- 现有的 API 接口

无需添加新的依赖。
