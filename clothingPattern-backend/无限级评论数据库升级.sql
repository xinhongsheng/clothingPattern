-- 无限级评论数据库升级脚本

-- 1. 添加 rootId 字段（根评论ID）
ALTER TABLE `comment` 
ADD COLUMN `rootId` bigint DEFAULT NULL COMMENT '根评论ID：null-主评论，非null-所有回复都指向根评论' AFTER `parentId`;

-- 2. 添加 replyToUserId 字段（被回复的用户ID）
ALTER TABLE `comment` 
ADD COLUMN `replyToUserId` bigint DEFAULT NULL COMMENT '被回复的用户ID：用于显示 @用户名' AFTER `rootId`;

-- 3. 添加索引优化查询性能
ALTER TABLE `comment` 
ADD INDEX `idx_rootId` (`rootId`);

-- 4. 更新现有数据：将所有回复的 rootId 设置为其父评论的 rootId（如果父评论也是回复）或父评论的 id（如果父评论是主评论）
UPDATE `comment` c1
LEFT JOIN `comment` c2 ON c1.parentId = c2.id
SET c1.rootId = CASE 
    WHEN c2.parentId IS NULL THEN c2.id  -- 父评论是主评论，rootId = 父评论ID
    ELSE c2.rootId                        -- 父评论是回复，rootId = 父评论的rootId
END
WHERE c1.parentId IS NOT NULL;

-- 5. 查看更新结果
SELECT 
    id,
    parentId,
    rootId,
    content,
    CASE 
        WHEN parentId IS NULL THEN '主评论'
        WHEN rootId IS NULL THEN '二级评论（未设置rootId）'
        ELSE CONCAT('回复（rootId=', rootId, '）')
    END as commentType
FROM comment
WHERE isDelete = 0
ORDER BY COALESCE(rootId, id), createTime;

