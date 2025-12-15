-- =====================================================
-- 协同过滤推荐系统所需表结构
-- =====================================================

-- 1. 用户行为表：记录谁对什么作品做了什么
DROP TABLE IF EXISTS `user_behavior`;
CREATE TABLE `user_behavior` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `userId` bigint NOT NULL COMMENT '用户ID',
  `patternId` bigint NOT NULL COMMENT '图案ID',
  `actionType` varchar(20) NOT NULL COMMENT '行为类型: VIEW(浏览), LIKE(点赞), DOWNLOAD(下载)',
  `weight` int NOT NULL DEFAULT 1 COMMENT '权重: 浏览=1, 下载=3, 点赞=5',
  `createTime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  INDEX `idx_userId`(`userId`),
  INDEX `idx_patternId`(`patternId`),
  INDEX `idx_userId_patternId`(`userId`, `patternId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户行为记录表';

-- 2. 物品相似度矩阵表：记录计算好的相似度
-- 这张表其实是算法的"缓存"，避免每次实时计算
DROP TABLE IF EXISTS `pattern_similarity`;
CREATE TABLE `pattern_similarity` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `patternIdA` bigint NOT NULL COMMENT '图案A的ID',
  `patternIdB` bigint NOT NULL COMMENT '图案B的ID',
  `similarity` double NOT NULL COMMENT '相似度分数 (0~1)',
  `updateTime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_ab` (`patternIdA`, `patternIdB`), -- 保证A和B只有一条记录
  INDEX `idx_patternIdA`(`patternIdA`),
  INDEX `idx_patternIdB`(`patternIdB`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='图案相似度矩阵表';
