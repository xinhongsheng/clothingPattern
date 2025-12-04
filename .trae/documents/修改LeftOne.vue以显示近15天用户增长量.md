## 修改LeftOne.vue以显示近15天用户增长量

### 1. 导入API函数

* 从`@/api/homeController`导入`getUserGrowth`函数

### 2. 移除静态数据和自动切换功能

* 删除或注释掉静态数据相关代码：`data`、`dataSeries`、`groupedData`、`groupedDataSeries`、`currentGroupIndex`

* 删除或注释掉自动切换数据的相关代码：`startDataSwitching`函数和`intervalId`变量

### 3. 添加数据获取逻辑

* 添加响应式数据`userGrowthData`来存储从后端获取的数据

* 添加`fetchUserGrowthData`函数，调用`getUserGrowth` API获取数据

### 4. 修改图表初始化和更新逻辑

* 修改`initChart1`函数，先获取数据再初始化图表

* 修改`updateChart`函数，使用从后端获取的数据配置图表

* 配置x轴显示日期，y轴显示用户数量

* 确保图表正确显示15天的数据

### 5. 保持样式不变

* 不修改任何样式代码

* 保持图表的颜色、布局等视觉效果不变

### 6. 移除不必要的功能

* 移除数据自动切换功能，因为我们只需要显示固定的15天数据

### 预期效果

* 图表将显示近15天的用户增长数据

* x轴显示日期（格式：YYYY-MM-DD）

* y轴显示每日新增用户数量

* 保持原有的蓝色渐变柱状图样式

* 支持全屏切换功能

### 文件修改

* 仅修改`LeftOne.vue`文件

* 不修改其他任何文件

