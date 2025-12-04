# 修复LeftTwo组件图表不显示问题

## 问题分析
1. 图表初始化后缺少resizeChart方法调用
2. 窗口大小变化时调用updateChart方法，而不是专用的resize方法
3. 数据获取失败时没有处理，可能导致图表为空
4. updateChart方法中没有检查chartInstance是否存在

## 修复方案
1. 添加resizeChart方法，专门用于调整图表大小
2. 修改窗口大小监听，调用resizeChart方法而非updateChart
3. 在fetchTargetAudienceData方法中处理数据为空的情况
4. 在updateChart方法中添加chartInstance存在性检查
5. 在组件挂载时调用resizeChart方法

## 实现步骤
1. 添加resizeChart方法
2. 修改onMounted和onBeforeUnmount中的事件监听
3. 修改fetchTargetAudienceData方法，添加数据为空时的处理
4. 在updateChart方法中添加chartInstance存在性检查
5. 确保图表在数据为空时也能正常显示

## 预期效果
图表能够正常显示后端返回的数据，支持窗口大小调整，在数据获取失败时也能显示空图表，不会导致组件崩溃。