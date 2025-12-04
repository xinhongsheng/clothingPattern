# RightTwo组件API响应数据访问方式修复计划

## 问题分析

根据request.ts文件的配置，axios返回的是完整的response对象，而不是只返回response.data。因此，在fetchData函数中，当前的API响应数据访问方式是错误的，导致无法获取到后端返回的真实数据。

## 修复方案

修改fetchData函数，使用正确的方式访问API响应数据：
- 将`response.code`修改为`response.data.code`
- 将`response.data`修改为`response.data.data`

## 代码修改点

1. **fetchData函数**：修改API响应数据的访问方式

## 预期效果

- 组件能够正确获取后端返回的数据
- 图表能够显示真实的风格偏好数据
- 保持原有图表样式、标签样式和布局不变

## 代码修改详情

修改fetchData函数中的API响应处理逻辑，使用正确的访问方式：

```javascript
const fetchData = async () => {
  try {
    const response = await getStylePreference()
    // 正确访问API响应数据，axios返回完整response对象
    if (response.data.code === 0 && response.data.data) {
      processData(response.data.data)
      updateChart()
    }
  } catch (error) {
    console.error('获取风格偏好数据失败:', error)
  }
}
```