需要修改LeftOne.vue组件，将直接使用request.get的方式改为使用homeController.ts中已定义的getUserGrowth函数。

修改步骤：
1. 移除直接引入的request实例
2. 引入homeController.ts中的getUserGrowth函数
3. 修改fetchUserGrowthData函数，使用引入的getUserGrowth函数
4. 调整API调用的响应处理，确保正确获取数据

具体修改内容：
- 删除第11行：`import request from '@/request'`
- 添加：`import { getUserGrowth } from '@/api/homeController'`
- 修改fetchUserGrowthData函数，将request.get替换为getUserGrowth()调用
- 调整响应处理逻辑，因为getUserGrowth函数返回的是Promise<API.BaseResponseListMapStringObject>类型