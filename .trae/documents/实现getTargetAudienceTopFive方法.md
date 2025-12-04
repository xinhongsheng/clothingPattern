# 实现getTargetAudienceTopFive方法

## 需求分析
需要实现`getTargetAudienceTopFive`方法，查询并返回前五的目标人群分布数据。

## 实现思路
1. 在PatternServiceImpl类中实现`getTargetAudienceTopFive`方法
2. 使用MyBatis-Plus的查询功能统计每个targetAudience的出现次数
3. 按出现次数降序排序
4. 取前5个结果
5. 返回格式为包含targetAudience和count的Map列表

## 实现步骤
1. 在PatternServiceImpl类中，修改`getTargetAudienceTopFive`方法
2. 使用QueryWrapper查询所有非空的targetAudience
3. 按targetAudience分组，统计每个分组的数量
4. 按数量降序排序，取前5个
5. 将结果转换为Map列表返回

## 预期返回格式
```json
[
  {"targetAudience": "年轻人", "count": 100},
  {"targetAudience": "中年人", "count": 80},
  {"targetAudience": "老年人", "count": 60},
  {"targetAudience": "儿童", "count": 40},
  {"targetAudience": "青少年", "count": 20}
]
```