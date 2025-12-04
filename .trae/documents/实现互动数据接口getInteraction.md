# 实现互动数据接口getInteraction

## 实现目标
根据作品的评论总数（占0.4）和点赞总数（占0.6）计算总分（总分100分），返回排名前五的作品，包括作品名称和所得分数。

## 实现步骤

1. **注入CommentService**
   - 在PatternServiceImpl类中注入CommentService，用于获取作品的评论统计信息

2. **实现getInteraction方法**
   - 查询所有审核通过的作品
   - 遍历每个作品，获取其评论总数和点赞总数
   - 计算总分：评论总数 * 0.4 + 点赞总数 * 0.6
   - 将总分转换为100分制（可选，根据实际数据分布调整）
   - 按总分降序排序，取前5名
   - 构建返回结果，包含作品名称和所得分数

3. **返回结果格式**
   - 返回List<Map<String, Object>>，每个Map包含两个字段：
     - patternName：作品名称
     - score：所得分数（保留两位小数）

## 代码实现

```java
@Override
public List<Map<String, Object>> getInteraction() {
    // 查询所有审核通过的作品
    QueryWrapper<Pattern> queryWrapper = new QueryWrapper<>();
    queryWrapper.eq("auditStatus", AuditStatusEnum.APPROVED.getValue());
    List<Pattern> patternList = this.list(queryWrapper);
    
    // 如果没有作品，返回空列表
    if (CollUtil.isEmpty(patternList)) {
        return new ArrayList<>();
    }
    
    // 计算每个作品的总分
    List<Map<String, Object>> scoreList = new ArrayList<>();
    for (Pattern pattern : patternList) {
        // 获取评论统计信息
        CommentStatisticsVO commentStats = commentService.getCommentStatistics(pattern.getId());
        int commentCount = commentStats.getTotalComments() != null ? commentStats.getTotalComments() : 0;
        
        // 获取点赞数
        long likeCount = likeService.getLikeCount(pattern.getId());
        
        // 计算总分（评论占0.4，点赞占0.6）
        double score = commentCount * 0.4 + likeCount * 0.6;
        
        // 构建结果Map
        Map<String, Object> scoreMap = new HashMap<>();
        scoreMap.put("patternName", pattern.getPatternName());
        scoreMap.put("score", Math.round(score * 100.0) / 100.0); // 保留两位小数
        
        scoreList.add(scoreMap);
    }
    
    // 按分数降序排序，取前5名
    return scoreList.stream()
            .sorted((a, b) -> Double.compare((Double) b.get("score"), (Double) a.get("score")))
            .limit(5)
            .collect(Collectors.toList());
}
```

## 注意事项

1. 需要确保CommentService已经被正确注入到PatternServiceImpl中
2. 计算总分时，根据实际数据分布可能需要调整权重或进行归一化处理
3. 返回结果中的分数保留两位小数，提高可读性
4. 考虑了没有作品的情况，返回空列表避免空指针异常