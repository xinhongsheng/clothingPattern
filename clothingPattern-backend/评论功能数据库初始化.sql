-- 评论功能数据库初始化脚本

-- 1. 创建评论表
CREATE TABLE IF NOT EXISTS `comment` (
  `id` bigint NOT NULL COMMENT '评论ID',
  `userId` bigint NOT NULL COMMENT '评论用户ID（关联user表）',
  `patternId` bigint NOT NULL COMMENT '被评论图案ID（关联pattern表）',
  `content` varchar(500) NOT NULL COMMENT '评论内容',
  `parentId` bigint DEFAULT NULL COMMENT '父评论ID：null-主评论，非null-回复某条评论',
  `createTime` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '评论时间',
  `updateTime` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间（编辑评论时更新）',
  `isDelete` tinyint DEFAULT '0' COMMENT '逻辑删除：0-未删除，1-已删除',
  `likeCount` int DEFAULT '0' COMMENT '点赞数',
  `replyCount` int DEFAULT '0' COMMENT '回复数',
  `topStatus` tinyint DEFAULT '0' COMMENT '置顶状态：0-否，1-是',
  `auditStatus` varchar(20) DEFAULT 'PENDING' COMMENT '审核状态：PENDING-待审核，APPROVED-已通过，REJECTED-已拒绝',
  PRIMARY KEY (`id`),
  KEY `idx_patternId` (`patternId`),
  KEY `idx_userId` (`userId`),
  KEY `idx_parentId` (`parentId`),
  KEY `idx_createTime` (`createTime`),
  KEY `idx_auditStatus` (`auditStatus`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='图案评论表';

-- 2. 创建评论点赞表
CREATE TABLE IF NOT EXISTS `comment_like` (
  `id` bigint NOT NULL COMMENT '评论点赞ID',
  `userId` bigint NOT NULL COMMENT '点赞用户ID',
  `commentId` bigint NOT NULL COMMENT '被点赞评论ID',
  `patternId` bigint NOT NULL COMMENT '图案ID（冗余字段，方便统计）',
  `createTime` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `isDelete` tinyint DEFAULT '0' COMMENT '逻辑删除：0-未删除，1-已删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_comment` (`userId`,`commentId`),
  KEY `idx_commentId` (`commentId`),
  KEY `idx_patternId` (`patternId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='评论点赞表';

-- 3. 插入测试数据（可选）
-- 注意：需要先确保 user 表和 pattern 表中有对应的数据

-- 插入主评论
INSERT INTO `comment` (`id`, `userId`, `patternId`, `content`, `parentId`, `likeCount`, `replyCount`, `topStatus`, `auditStatus`)
VALUES 
(1, 1991067449709658113, 1992847245649223682, '这个图案设计真的很棒！颜色搭配很和谐。', NULL, 5, 2, 0, 'APPROVED'),
(2, 1991067449709658113, 1992847245649223682, '非常喜欢这个风格，期待更多作品！', NULL, 3, 1, 0, 'APPROVED'),
(3, 1991067449709658113, 1992847245649223682, '这个图案很有创意，适合做T恤图案。', NULL, 8, 0, 0, 'APPROVED');

-- 插入回复评论
INSERT INTO `comment` (`id`, `userId`, `patternId`, `content`, `parentId`, `likeCount`, `replyCount`, `topStatus`, `auditStatus`)
VALUES 
(4, 1991067449709658113, 1992847245649223682, '我也这么觉得，特别喜欢这个风格！', 1, 2, 0, 0, 'APPROVED'),
(5, 1991067449709658113, 1992847245649223682, '同意，颜色搭配确实很棒！', 1, 1, 0, 0, 'APPROVED'),
(6, 1991067449709658113, 1992847245649223682, '期待作者的新作品！', 2, 0, 0, 0, 'APPROVED');

-- 插入点赞记录
INSERT INTO `comment_like` (`id`, `userId`, `commentId`, `patternId`, `isDelete`)
VALUES 
(1, 1991067449709658113, 1, 1992847245649223682, 0),
(2, 1991067449709658113, 2, 1992847245649223682, 0),
(3, 1991067449709658113, 4, 1992847245649223682, 0);

-- 4. 查询验证
-- 查询某个图案的所有评论
SELECT 
    c.*,
    u.userName,
    u.userAvatar
FROM comment c
LEFT JOIN user u ON c.userId = u.id
WHERE c.patternId = 1992847245649223682
  AND c.isDelete = 0
  AND c.auditStatus = 'APPROVED'
ORDER BY c.topStatus DESC, c.createTime DESC;

-- 查询某个评论的所有回复
SELECT 
    c.*,
    u.userName,
    u.userAvatar
FROM comment c
LEFT JOIN user u ON c.userId = u.id
WHERE c.parentId = 1
  AND c.isDelete = 0
  AND c.auditStatus = 'APPROVED'
ORDER BY c.createTime ASC;

-- 查询某个图案的评论统计
SELECT 
    COUNT(*) as totalComments,
    SUM(CASE WHEN parentId IS NULL THEN 1 ELSE 0 END) as mainComments,
    SUM(likeCount) as totalLikes
FROM comment
WHERE patternId = 1992847245649223682
  AND isDelete = 0
  AND auditStatus = 'APPROVED';

-- 查询用户是否点赞某个评论
SELECT COUNT(*) as liked
FROM comment_like
WHERE userId = 1991067449709658113
  AND commentId = 1
  AND isDelete = 0;

-- 5. 清理测试数据（可选）
-- DELETE FROM comment_like WHERE patternId = 1992847245649223682;
-- DELETE FROM comment WHERE patternId = 1992847245649223682;

