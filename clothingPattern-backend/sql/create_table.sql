-- 用户表
create table if not exists user
(
    id           bigint auto_increment comment 'id' primary key,
    userAccount  varchar(256)                           not null comment '账号',
    userPassword varchar(512)                           not null comment '密码',
    userName     varchar(256)                           null comment '用户昵称',
    userAvatar   varchar(1024)                          null comment '用户头像',
    userProfile  varchar(512)                           null comment '用户简介',
    userRole     varchar(256) default 'user'            not null comment '用户角色：user/admin',
    editTime     datetime     default CURRENT_TIMESTAMP not null comment '编辑时间',
    createTime   datetime     default CURRENT_TIMESTAMP not null comment '创建时间',
    updateTime   datetime     default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    isDelete     tinyint      default 0                 not null comment '是否删除',
    UNIQUE KEY uk_userAccount (userAccount),
    INDEX idx_userName (userName)
    ) comment '用户' collate = utf8mb4_unicode_ci;

CREATE TABLE `pattern`  (
                            `id` bigint NOT NULL AUTO_INCREMENT COMMENT '图案唯一主键ID',
                            `userId` bigint NOT NULL COMMENT '关联用户ID（创建者）',
                            `patternName` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '图案名称',
                            `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '图案描述（用户输入的文字信息）',
                            `generationType` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '生成类型：TEXT_GENERATED（文字生成）、IMAGE_REFERENCED（图片参考生成）、BAILIAN_GENERATED（百炼生成）',
                            `referenceImageUrl` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '参考图片URL（图片参考生成时使用）',
                            `patternUrl` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '生成图案的最终URL（云存储地址）',
                            `thumbUrl` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '图案缩略图URL（列表页展示用）',
                            `fileSize` int NULL DEFAULT NULL COMMENT '图案文件大小（字节）',
                            `fileType` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '图案文件类型（如image/png）',
                            `style` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '图案风格（元数据标签：复古、简约、卡通等）',
                            `season` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '适用季节（元数据标签：春季、夏季等）',
                            `targetAudience` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '目标受众（元数据标签：儿童、成人等）',
                            `generationParams` json NULL COMMENT '生成参数（JSON格式：如negative_prompt、size等）',
                            `auditStatus` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT 'PENDING' COMMENT '审核状态：PENDING（待审核）、APPROVED（已通过）、REJECTED（已拒绝）',
                            `auditTime` datetime NULL DEFAULT NULL COMMENT '审核时间',
                            `auditorId` bigint NULL DEFAULT NULL COMMENT '审核员ID（关联管理员表）',
                            `rejectReason` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '拒绝原因（审核拒绝时填写）',
                            `likeCount` int NOT NULL DEFAULT 0 COMMENT '点赞数',
                            `createTime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                            `updateTime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                            `isDelete` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
                            PRIMARY KEY (`id`) USING BTREE,
                            INDEX `idx_userId`(`userId` ASC) USING BTREE,
                            INDEX `idx_auditStatus`(`auditStatus` ASC) USING BTREE,
                            INDEX `idx_createTime`(`createTime` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1991861493981306882 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '服装图案表（智能图案生成模块核心表）' ROW_FORMAT = Dynamic;

SET FOREIGN_KEY_CHECKS = 1;

CREATE TABLE `userLike` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '点赞记录ID',
    `userId` bigint NOT NULL COMMENT '点赞用户ID（关联user表）',
    `patternId` bigint NOT NULL COMMENT '被点赞图案ID（关联pattern表）',
    `createTime` datetime DEFAULT CURRENT_TIMESTAMP NOT NULL COMMENT '创建时间',
    `isDelete` tinyint DEFAULT 0 NOT NULL COMMENT '逻辑删除：0-未删除（有效），1-已删除',
    PRIMARY KEY (`id`) USING BTREE,
    UNIQUE KEY `uk_user_pattern` (`userId`, `patternId`),
    INDEX `idx_pattern_id` (`patternId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='图案点赞表';


CREATE TABLE `comment` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '评论ID',
    `userId` bigint NOT NULL COMMENT '评论用户ID（关联user表）',
    `patternId` bigint NOT NULL COMMENT '被评论图案ID（关联pattern表）',
    `content` text NOT NULL COMMENT '评论内容',
    `parentId` bigint NULL DEFAULT NULL COMMENT '父评论ID：null-主评论，非null-回复某条评论',
    `createTime` datetime DEFAULT CURRENT_TIMESTAMP NOT NULL COMMENT '评论时间',
    `updateTime` datetime DEFAULT CURRENT_TIMESTAMP NOT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间（编辑评论时更新）',
    `isDelete` tinyint DEFAULT 0 NOT NULL COMMENT '逻辑删除：0-未删除，1-已删除',
    PRIMARY KEY (`id`) USING BTREE,
    -- 索引：优化查询效率
    INDEX `idx_pattern_id` (`patternId`), -- 按图案查询所有评论
    INDEX `idx_parent_id` (`parentId`) -- 按父评论ID查询回复（层级评论）
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='图案评论表';

ALTER TABLE `userLike` RENAME TO `user_like`;

-- 建议在 user_like 表添加 updateTime
ALTER TABLE `user_like`
    ADD COLUMN `updateTime` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间';

-- 添加复合索引
ALTER TABLE `user_like` ADD INDEX `idx_user_pattern_status` (`userId`, `patternId`, `isDelete`);

ALTER TABLE `comment`
    ADD COLUMN `likeCount` int DEFAULT 0 COMMENT '点赞数',
    ADD COLUMN `replyCount` int DEFAULT 0 COMMENT '回复数',
    ADD COLUMN `topStatus` tinyint DEFAULT 0 COMMENT '置顶状态：0-否，1-是',
    ADD COLUMN `auditStatus` varchar(50) DEFAULT 'PENDING' COMMENT '审核状态：PENDING-待审核，APPROVED-已通过，REJECTED-已拒绝',
    ADD INDEX `idx_user_id` (`userId`),
    ADD INDEX `idx_create_time` (`createTime`);

-- 创建评论点赞表
CREATE TABLE `comment_like` (
                                `id` bigint NOT NULL AUTO_INCREMENT COMMENT '评论点赞ID',
                                `userId` bigint NOT NULL COMMENT '点赞用户ID',
                                `commentId` bigint NOT NULL COMMENT '被点赞评论ID',
                                `createTime` datetime DEFAULT CURRENT_TIMESTAMP NOT NULL COMMENT '创建时间',
                                `isDelete` tinyint DEFAULT 0 NOT NULL COMMENT '逻辑删除',
                                PRIMARY KEY (`id`),
                                UNIQUE KEY `uk_user_comment` (`userId`, `commentId`),
                                INDEX `idx_comment_id` (`commentId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='评论点赞表';

-- 确保索引优化
ALTER TABLE `comment`
    ADD INDEX `idx_pattern_create_time` (`patternId`, `createTime`),
    ADD INDEX `idx_pattern_parent` (`patternId`, `parentId`);

-- 评论点赞表也建议添加图案ID索引（可选，便于管理）
ALTER TABLE `comment_like`
    ADD COLUMN `patternId` bigint COMMENT '图案ID（冗余字段，便于查询）',
    ADD INDEX `idx_pattern_id` (`patternId`);

-- 更新评论点赞表数据（如果已有数据）
UPDATE comment_like cl
    JOIN comment c ON cl.commentId = c.id
SET cl.patternId = c.patternId
WHERE cl.patternId IS NULL;


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


-- 文章资讯功能
CREATE TABLE `articleCategory` (
                                   `id` bigint NOT NULL AUTO_INCREMENT COMMENT '分类ID',
                                   `categoryName` varchar(100) NOT NULL COMMENT '分类名称',
                                   `categoryDesc` varchar(500) DEFAULT NULL COMMENT '分类描述',
                                   `icon` varchar(255) DEFAULT NULL COMMENT '分类图标',
                                   `sortOrder` int DEFAULT 0 COMMENT '排序字段，越大越靠前',
                                   `status` tinyint DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
                                   `createTime` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                   `updateTime` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                                   `isDelete` tinyint DEFAULT 0 COMMENT '逻辑删除',
                                   PRIMARY KEY (`id`),
                                   INDEX `idxSortOrder` (`sortOrder`),
                                   INDEX `idxStatus` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='文章分类表';

CREATE TABLE `article` (
                           `id` bigint NOT NULL AUTO_INCREMENT COMMENT '文章ID',
                           `categoryId` bigint NOT NULL COMMENT '分类ID',
                           `title` varchar(200) NOT NULL COMMENT '文章标题',
                           `coverImage` varchar(500) DEFAULT NULL COMMENT '封面图',
                           `summary` varchar(500) DEFAULT NULL COMMENT '文章摘要',
                           `content` longtext COMMENT '文章内容',
                           `author` varchar(100) DEFAULT NULL COMMENT '作者',
                           `source` varchar(100) DEFAULT NULL COMMENT '来源',
                           `tags` varchar(255) DEFAULT NULL COMMENT '标签，逗号分隔',
                           `viewCount` int DEFAULT 0 COMMENT '浏览量',
                           `likeCount` int DEFAULT 0 COMMENT '点赞量',
                           `commentCount` int DEFAULT 0 COMMENT '评论量',
                           `shareCount` int DEFAULT 0 COMMENT '分享量',
                           `collectCount` int DEFAULT 0 COMMENT '收藏量',
                           `isTop` tinyint DEFAULT 0 COMMENT '是否置顶：0-否，1-是',
                           `isHot` tinyint DEFAULT 0 COMMENT '是否热门：0-否，1-是',
                           `isRecommend` tinyint DEFAULT 0 COMMENT '是否推荐：0-否，1-是',
                           `status` varchar(50) DEFAULT 'DRAFT' COMMENT '状态：DRAFT-草稿，PUBLISHED-已发布，OFFLINE-已下架',
                           `auditStatus` varchar(50) DEFAULT 'PENDING' COMMENT '审核状态：PENDING-待审核，APPROVED-已通过，REJECTED-已拒绝',
                           `publishTime` datetime DEFAULT NULL COMMENT '发布时间',
                           `createTime` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                           `updateTime` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                           `isDelete` tinyint DEFAULT 0 COMMENT '逻辑删除',
                           PRIMARY KEY (`id`),
                           INDEX `idxCategoryId` (`categoryId`),
                           INDEX `idxPublishTime` (`publishTime`),
                           INDEX `idxIsTop` (`isTop`),
                           INDEX `idxIsHot` (`isHot`),
                           INDEX `idxIsRecommend` (`isRecommend`),
                           INDEX `idxStatus` (`status`),
                           INDEX `idxAuditStatus` (`auditStatus`),
                           INDEX `idxViewCount` (`viewCount`),
                           INDEX `idxCreateTime` (`createTime`),
                           FULLTEXT KEY `ftTitleSummary` (`title`, `summary`) -- 全文索引
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='文章资讯表';

CREATE TABLE `articleLike` (
                               `id` bigint NOT NULL AUTO_INCREMENT COMMENT '点赞ID',
                               `userId` bigint NOT NULL COMMENT '用户ID',
                               `articleId` bigint NOT NULL COMMENT '文章ID',
                               `createTime` datetime DEFAULT CURRENT_TIMESTAMP NOT NULL COMMENT '创建时间',
                               `isDelete` tinyint DEFAULT 0 NOT NULL COMMENT '逻辑删除',
                               PRIMARY KEY (`id`),
                               UNIQUE KEY `ukUserArticle` (`userId`, `articleId`),
                               INDEX `idxArticleId` (`articleId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='文章点赞表';

CREATE TABLE `articleCollect` (
                                  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '收藏ID',
                                  `userId` bigint NOT NULL COMMENT '用户ID',
                                  `articleId` bigint NOT NULL COMMENT '文章ID',
                                  `createTime` datetime DEFAULT CURRENT_TIMESTAMP NOT NULL COMMENT '创建时间',
                                  `isDelete` tinyint DEFAULT 0 NOT NULL COMMENT '逻辑删除',
                                  PRIMARY KEY (`id`),
                                  UNIQUE KEY `ukUserArticle` (`userId`, `articleId`),
                                  INDEX `idxArticleId` (`articleId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='文章收藏表';

-- 轮播图
CREATE TABLE `banner` (
                          `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
                          `title` varchar(255) DEFAULT NULL COMMENT '标题',
                          `imageUrl` varchar(512) DEFAULT NULL COMMENT '图片URL',
                          `linkUrl` varchar(512) DEFAULT NULL COMMENT '链接URL',
                          `sortOrder` int DEFAULT '0' COMMENT '排序',
                          `status` tinyint DEFAULT '1' COMMENT '状态：0-禁用，1-启用',
                          `createTime` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                          `updateTime` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                          `isDelete` tinyint DEFAULT '0' COMMENT '逻辑删除：0-未删除，1-已删除',
                          PRIMARY KEY (`id`),
                          KEY `idx_sort_order` (`sortOrder`),
                          KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='轮播图表';


-- AI试衣
CREATE TABLE `try_on_task` (
                               `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
                               `userId` bigint DEFAULT NULL COMMENT '关联用户ID',
                               `personImageUrl` varchar(512) NOT NULL COMMENT '人物图片公网URL',
                               `topGarmentUrl` varchar(512) DEFAULT NULL COMMENT '上装图片公网URL',
                               `bottomGarmentUrl` varchar(512) DEFAULT NULL COMMENT '下装图片公网URL',
                               `dashscopeTaskId` varchar(128) NOT NULL COMMENT '阿里云异步任务ID',
                               `taskStatus` varchar(32) NOT NULL DEFAULT 'PENDING' COMMENT '任务状态：PENDING/SUCCEEDED/FAILED',
                               `resultImageUrl` varchar(512) DEFAULT NULL COMMENT '试衣结果图片URL',
                               `createTime` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
                               `updateTime` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                               PRIMARY KEY (`id`),
                               KEY `idx_task_id` (`dashscopeTaskId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

ALTER TABLE `try_on_task`
    ADD COLUMN `submitTime` datetime DEFAULT NULL COMMENT '任务提交时间',
ADD COLUMN `scheduledTime` datetime DEFAULT NULL COMMENT '任务执行时间',
ADD COLUMN `endTime` datetime DEFAULT NULL COMMENT '任务完成时间',
ADD COLUMN `errorCode` varchar(64) DEFAULT NULL COMMENT '错误码（失败时）',
ADD COLUMN `errorMessage` varchar(512) DEFAULT NULL COMMENT '错误详情（失败时）',
ADD COLUMN `localResultUrl` varchar(512) DEFAULT NULL COMMENT '本地OSS保存的结果图片URL（永久有效）';


-- 多图融合
CREATE TABLE `image_fusion_task` (
                                     `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
                                     `userId` bigint NOT NULL COMMENT '用户ID',
                                     `dashscopeTaskId` varchar(64) NOT NULL COMMENT '通义万相任务ID',
                                     `prompt` varchar(2000) NOT NULL COMMENT '正向提示词',
                                     `negativePrompt` varchar(500) DEFAULT NULL COMMENT '反向提示词',
                                     `imageUrls` varchar(2048) NOT NULL COMMENT '输入图片URLs（逗号分隔）',
                                     `parameters` varchar(1024) DEFAULT NULL COMMENT '任务参数（JSON格式，如size、n等）',
                                     `taskStatus` varchar(32) NOT NULL COMMENT '任务状态：PENDING/RUNNING/SUCCEEDED/FAILED等',
                                     `submitTime` datetime DEFAULT NULL COMMENT '任务提交时间',
                                     `scheduledTime` datetime DEFAULT NULL COMMENT '任务执行时间',
                                     `endTime` datetime DEFAULT NULL COMMENT '任务完成时间',
                                     `errorCode` varchar(64) DEFAULT NULL COMMENT '错误码（失败时）',
                                     `errorMessage` varchar(512) DEFAULT NULL COMMENT '错误详情（失败时）',

    -- 新增：合并结果图片字段（多值用逗号分隔）
                                     `origPrompts` varchar(4000) DEFAULT '' COMMENT '结果图片原始提示词（逗号分隔，对应多图）',
                                     `tempImageUrls` varchar(2048) DEFAULT '' COMMENT '临时图片URL（逗号分隔，24小时有效）',
                                     `localImageUrls` varchar(2048) DEFAULT '' COMMENT '本地OSS永久URL（逗号分隔）',
                                     `sorts` varchar(64) DEFAULT '' COMMENT '结果图片排序（逗号分隔，如1,2,3）',

                                     `createTime` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                     `updateTime` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                                     PRIMARY KEY (`id`),
                                     KEY `idx_task_id` (`dashscopeTaskId`),
                                     KEY `idx_user_id` (`userId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='多图融合任务表（含结果）';



SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- 清空原有pattern表数据
TRUNCATE TABLE `pattern`;

-- 重新插入差异化数据（聚焦过去一周，风格数量有明显高低）
CREATE TABLE IF NOT EXISTS `pattern` (
                                         `id` bigint NOT NULL AUTO_INCREMENT COMMENT '图案唯一主键ID',
                                         `userId` bigint NOT NULL COMMENT '关联用户ID（创建者）',
                                         `patternName` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '图案名称',
                                         `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '图案描述（用户输入的文字信息）',
                                         `generationType` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '生成类型：TEXT_GENERATED（文字生成）、IMAGE_REFERENCED（图片参考生成）、BAILIAN_GENERATED（百炼生成）',
                                         `referenceImageUrl` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '参考图片URL（图片参考生成时使用）',
                                         `patternUrl` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '生成图案的最终URL（云存储地址）',
                                         `thumbUrl` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '图案缩略图URL（列表页展示用）',
                                         `fileSize` int NULL DEFAULT NULL COMMENT '图案文件大小（字节）',
                                         `fileType` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '图案文件类型（如image/png）',
                                         `style` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '图案风格（元数据标签：复古、简约、卡通等）',
                                         `season` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '适用季节（元数据标签：春季、夏季等）',
                                         `targetAudience` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '目标受众（元数据标签：儿童、成人等）',
                                         `generationParams` json NULL COMMENT '生成参数（JSON格式：如negative_prompt、size等）',
                                         `auditStatus` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT 'PENDING' COMMENT '审核状态：PENDING（待审核）、APPROVED（已通过）、REJECTED（已拒绝）',
                                         `auditTime` datetime NULL DEFAULT NULL COMMENT '审核时间',
                                         `auditorId` bigint NULL DEFAULT NULL COMMENT '审核员ID（关联管理员表）',
                                         `rejectReason` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '拒绝原因（审核拒绝时填写）',
                                         `likeCount` int NOT NULL DEFAULT 0 COMMENT '点赞数',
                                         `createTime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                         `updateTime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                                         `isDelete` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
                                         PRIMARY KEY (`id`) USING BTREE,
                                         INDEX `idx_userId`(`userId` ASC) USING BTREE,
                                         INDEX `idx_auditStatus`(`auditStatus` ASC) USING BTREE,
                                         INDEX `idx_createTime`(`createTime` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1996442354772226050 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '服装图案表（智能图案生成模块核心表）' ROW_FORMAT = DYNAMIC;

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

-- 用户表添加省份字段
ALTER TABLE `user` 
ADD COLUMN `province` varchar(50) DEFAULT NULL COMMENT '所在省份' AFTER `userProfile`;

-- 添加省份索引（便于统计查询）
ALTER TABLE `user` ADD INDEX `idx_province` (`province`);
