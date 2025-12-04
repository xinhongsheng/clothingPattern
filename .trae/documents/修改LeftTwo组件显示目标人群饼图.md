# 修改LeftTwo组件显示目标人群饼图

## 需求分析
1. LeftTwo组件当前使用模拟数据和定时器切换数据
2. 需要调用后端的getTargetTopFive接口获取实际数据
3. 将后端返回的数据转换为ECharts饼图需要的格式
4. 在大屏左边第二个饼图中正常显示
5. 不修改样式，不修改大屏布局

## 实现思路
1. 导入getTargetTopFive接口
2. 移除模拟数据和定时器逻辑
3. 添加数据获取和处理函数
4. 将后端返回的数据转换为ECharts饼图需要的格式
5. 修改图表标题为"目标人群分布"
6. 确保图表能正常初始化和响应窗口大小变化

## 实现步骤
1. 在LeftTwo.vue中导入getTargetTopFive接口
2. 删除模拟数据dataGroups和currentGroupIndex变量
3. 删除startDataSwitching函数和相关定时器逻辑
4. 添加fetchTargetAudienceData函数获取后端数据
5. 修改updateChart函数，使用实际数据
6. 修改图表名称为"目标人群分布"
7. 确保图表初始化时调用数据获取函数

## 预期效果
饼图显示后端返回的目标人群数据，标题为"目标人群分布"，不切换数据，保持静态显示，样式和布局不变。