-- 添加点赞数字段到 pattern 表
-- 执行日期：2025-11-25

-- 1. 添加 likeCount 字段
ALTER TABLE `pattern` 
ADD COLUMN `likeCount` int NOT NULL DEFAULT 0 COMMENT '点赞数' AFTER `rejectReason`;

-- 2. 为已存在的图案初始化点赞数（从 user_like 表统计）
UPDATE `pattern` p
SET p.likeCount = (
    SELECT COUNT(*) 
    FROM user_like ul 
    WHERE ul.patternId = p.id 
    AND ul.isDelete = 0
)
WHERE p.isDelete = 0;
