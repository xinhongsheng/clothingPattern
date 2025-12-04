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
                            `generationType` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '生成类型：TEXT_GENERATED（文字生成）、IMAGE_REFERENCED（图片参考生成）',
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
                                         `generationType` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '生成类型：TEXT_GENERATED（文字生成）、IMAGE_REFERENCED（图片参考生成）、MJ_GENERATED（MJ生成）',
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

-- 1. 风格：可爱（20条，过去一周）
INSERT INTO `pattern` (`id`, `userId`, `patternName`, `description`, `generationType`, `referenceImageUrl`, `patternUrl`, `thumbUrl`, `fileSize`, `fileType`, `style`, `season`, `targetAudience`, `generationParams`, `auditStatus`, `auditTime`, `auditorId`, `rejectReason`, `likeCount`, `createTime`, `updateTime`, `isDelete`)
VALUES
-- 可爱风格（20条，时间分布在2025-11-28至2025-12-04）
(1994273567843287041, 1991067449709658113, 'MJ-小狗图案', '可爱小狗', 'MJ_GENERATED', NULL, 'https://platform.cdn.zhishuyun.com/midjourney/af56a590-b64e-43fb-a8fb-d544279c7e7c.png', 'https://platform.cdn.zhishuyun.com/midjourney/af56a590-b64e-43fb-a8fb-d544279c7e7c.png?imageMogr2/thumbnail/!50p', NULL, NULL, '可爱', '冬季', '儿童', '{\"task_id\": \"af56a590-b64e-43fb-a8fb-d544279c7e7c\"}', 'APPROVED', NULL, NULL, NULL, 1, '2025-11-28 13:14:05', '2025-12-04 16:46:47', 0),
(1994298187375968258, 1991067449709658113, 'MJ-小猫图案', '可爱小猫', 'MJ_GENERATED', NULL, 'https://platform.cdn.zhishuyun.com/midjourney/a63e6c05-e12e-4dcf-82cd-553ed60c2c02.png', 'https://platform.cdn.zhishuyun.com/midjourney/a63e6c05-e12e-4dcf-82cd-553ed60c2c02.png?imageMogr2/thumbnail/!50p', NULL, NULL, '可爱', '四季', '通用', '{\"task_id\": \"a63e6c05-e12e-4dcf-82cd-553ed60c2c02\"}', 'APPROVED', '2025-11-29 17:19:38', 1991067449709658113, '管理员自动过审', 1, '2025-11-28 14:51:54', '2025-12-04 16:46:47', 0),
(1995363570191134721, 1991067449709658113, '大黄狗', '大黄狗', 'TEXT_GENERATED', NULL, 'https://clothingpattern-1317715434.cos.ap-guangzhou.myqcloud.com/pattern/1991067449709658113/1764566720166.webp', 'https://clothingpattern-1317715434.cos.ap-guangzhou.myqcloud.com/pattern/1991067449709658113/1764566720166_thumbnail.png', 1651045, 'image/png', '可爱', '春季', '儿童', NULL, 'APPROVED', NULL, NULL, NULL, 0, '2025-12-01 13:25:21', '2025-12-03 09:29:43', 1),
(1995370446375047169, 1991067449709658113, '大黄狗22', '大黄狗', 'TEXT_GENERATED', NULL, 'https://clothingpattern-1317715434.cos.ap-guangzhou.myqcloud.com/pattern/1991067449709658113/1764568359981.webp', 'https://clothingpattern-1317715434.cos.ap-guangzhou.myqcloud.com/pattern/1991067449709658113/1764568359981_thumbnail.png', 1691499, 'image/png', '可爱', '夏季', '儿童', NULL, 'APPROVED', '2025-12-01 13:52:41', 1991067449709658113, '管理员自动过审', 0, '2025-12-01 13:52:41', '2025-12-03 09:29:41', 1),
(1995371109507084289, 1991067449709658113, '大黄狗22', '大黄狗', 'TEXT_GENERATED', NULL, 'https://clothingpattern-1317715434.cos.ap-guangzhou.myqcloud.com/pattern/1991067449709658113/1764568517957.webp', 'https://clothingpattern-1317715434.cos.ap-guangzhou.myqcloud.com/pattern/1991067449709658113/1764568517957_thumbnail.png', 1920238, 'image/png', '可爱', '夏季', '儿童', NULL, 'APPROVED', '2025-12-01 13:55:19', 1991067449709658113, '管理员自动过审', 0, '2025-12-01 13:55:19', '2025-12-03 09:29:40', 1),
(1995371226180038658, 1991067449709658113, '小猫咪11', '小猫咪', 'TEXT_GENERATED', NULL, 'https://clothingpattern-1317715434.cos.ap-guangzhou.myqcloud.com/pattern/1991067449709658113/1764568546609.webp', 'https://clothingpattern-1317715434.cos.ap-guangzhou.myqcloud.com/pattern/1991067449709658113/1764568546609_thumbnail.png', 1452171, 'image/png', '可爱', '夏季', '儿童', NULL, 'APPROVED', '2025-12-01 13:55:47', 1991067449709658113, '管理员自动过审', 0, '2025-12-01 13:55:47', '2025-12-03 09:29:40', 1),
(1995376574878171138, 1991067449709658113, '大黄狗', '大黄狗', 'TEXT_GENERATED', NULL, 'https://clothingpattern-1317715434.cos.ap-guangzhou.myqcloud.com/pattern/1991067449709658113/1764569820764.webp', 'https://clothingpattern-1317715434.cos.ap-guangzhou.myqcloud.com/pattern/1991067449709658113/1764569820764_thumbnail.png', 1770147, 'image/png', '可爱', '夏季', '青少年', NULL, 'APPROVED', '2025-12-01 14:17:02', 1991067449709658113, '管理员自动过审', 0, '2025-12-01 14:17:02', '2025-12-03 09:29:39', 1),
(1996026368965369857, 1991067449709658113, '小猫咪', '小猫咪', 'TEXT_GENERATED', NULL, 'https://clothingpattern-1317715434.cos.ap-guangzhou.myqcloud.com/pattern/1991067449709658113/1764724744069.webp', 'https://clothingpattern-1317715434.cos.ap-guangzhou.myqcloud.com/pattern/1991067449709658113/1764724744069_thumbnail.png', 1690533, 'image/png', '可爱', '四季', '通用', NULL, 'APPROVED', '2025-12-03 09:19:05', 1991067449709658113, '管理员自动过审', 0, '2025-12-03 09:19:05', '2025-12-03 09:29:38', 1),
-- 补充可爱风格剩余12条（简化示例，实际可按需扩展）
(2000000000000000001, 1991067449709658113, '可爱水豚1', '水豚噜噜', 'TEXT_GENERATED', NULL, 'https://clothingpattern-1317715434.cos.ap-guangzhou.myqcloud.com/pattern/1991067449709658113/1764724744069.webp', 'https://clothingpattern-1317715434.cos.ap-guangzhou.myqcloud.com/pattern/1991067449709658113/1764724744069_thumbnail.png', 1690533, 'image/png', '可爱', '春季', '儿童', NULL, 'APPROVED', NULL, NULL, NULL, 0, '2025-11-29 10:00:00', '2025-11-29 10:00:00', 0),
(2000000000000000002, 1991067449709658113, '可爱水豚2', '水豚噜噜', 'TEXT_GENERATED', NULL, 'https://clothingpattern-1317715434.cos.ap-guangzhou.myqcloud.com/pattern/1991067449709658113/1764724744069.webp', 'https://clothingpattern-1317715434.cos.ap-guangzhou.myqcloud.com/pattern/1991067449709658113/1764724744069_thumbnail.png', 1690533, 'image/png', '可爱', '春季', '儿童', NULL, 'APPROVED', NULL, NULL, NULL, 0, '2025-11-29 11:00:00', '2025-11-29 11:00:00', 0),
(2000000000000000003, 1991067449709658113, '可爱水豚3', '水豚噜噜', 'TEXT_GENERATED', NULL, 'https://clothingpattern-1317715434.cos.ap-guangzhou.myqcloud.com/pattern/1991067449709658113/1764724744069.webp', 'https://clothingpattern-1317715434.cos.ap-guangzhou.myqcloud.com/pattern/1991067449709658113/1764724744069_thumbnail.png', 1690533, 'image/png', '可爱', '春季', '儿童', NULL, 'APPROVED', NULL, NULL, NULL, 0, '2025-11-30 10:00:00', '2025-11-30 10:00:00', 0),
(2000000000000000004, 1991067449709658113, '可爱水豚4', '水豚噜噜', 'TEXT_GENERATED', NULL, 'https://clothingpattern-1317715434.cos.ap-guangzhou.myqcloud.com/pattern/1991067449709658113/1764724744069.webp', 'https://clothingpattern-1317715434.cos.ap-guangzhou.myqcloud.com/pattern/1991067449709658113/1764724744069_thumbnail.png', 1690533, 'image/png', '可爱', '春季', '儿童', NULL, 'APPROVED', NULL, NULL, NULL, 0, '2025-11-30 11:00:00', '2025-11-30 11:00:00', 0),
(2000000000000000005, 1991067449709658113, '可爱兔子1', '兔子图案', 'TEXT_GENERATED', NULL, 'https://clothingpattern-1317715434.cos.ap-guangzhou.myqcloud.com/pattern/1991067449709658113/1764724744069.webp', 'https://clothingpattern-1317715434.cos.ap-guangzhou.myqcloud.com/pattern/1991067449709658113/1764724744069_thumbnail.png', 1690533, 'image/png', '可爱', '春季', '儿童', NULL, 'APPROVED', NULL, NULL, NULL, 0, '2025-12-01 10:00:00', '2025-12-01 10:00:00', 0),
(2000000000000000006, 1991067449709658113, '可爱兔子2', '兔子图案', 'TEXT_GENERATED', NULL, 'https://clothingpattern-1317715434.cos.ap-guangzhou.myqcloud.com/pattern/1991067449709658113/1764724744069.webp', 'https://clothingpattern-1317715434.cos.ap-guangzhou.myqcloud.com/pattern/1991067449709658113/1764724744069_thumbnail.png', 1690533, 'image/png', '可爱', '春季', '儿童', NULL, 'APPROVED', NULL, NULL, NULL, 0, '2025-12-01 11:00:00', '2025-12-01 11:00:00', 0),
(2000000000000000007, 1991067449709658113, '可爱兔子3', '兔子图案', 'TEXT_GENERATED', NULL, 'https://clothingpattern-1317715434.cos.ap-guangzhou.myqcloud.com/pattern/1991067449709658113/1764724744069.webp', 'https://clothingpattern-1317715434.cos.ap-guangzhou.myqcloud.com/pattern/1991067449709658113/1764724744069_thumbnail.png', 1690533, 'image/png', '可爱', '春季', '儿童', NULL, 'APPROVED', NULL, NULL, NULL, 0, '2025-12-02 10:00:00', '2025-12-02 10:00:00', 0),
(2000000000000000008, 1991067449709658113, '可爱兔子4', '兔子图案', 'TEXT_GENERATED', NULL, 'https://clothingpattern-1317715434.cos.ap-guangzhou.myqcloud.com/pattern/1991067449709658113/1764724744069.webp', 'https://clothingpattern-1317715434.cos.ap-guangzhou.myqcloud.com/pattern/1991067449709658113/1764724744069_thumbnail.png', 1690533, 'image/png', '可爱', '春季', '儿童', NULL, 'APPROVED', NULL, NULL, NULL, 0, '2025-12-02 11:00:00', '2025-12-02 11:00:00', 0),
(2000000000000000009, 1991067449709658113, '可爱小熊1', '小熊图案', 'TEXT_GENERATED', NULL, 'https://clothingpattern-1317715434.cos.ap-guangzhou.myqcloud.com/pattern/1991067449709658113/1764724744069.webp', 'https://clothingpattern-1317715434.cos.ap-guangzhou.myqcloud.com/pattern/1991067449709658113/1764724744069_thumbnail.png', 1690533, 'image/png', '可爱', '冬季', '儿童', NULL, 'APPROVED', NULL, NULL, NULL, 0, '2025-12-03 10:00:00', '2025-12-03 10:00:00', 0),
(2000000000000000010, 1991067449709658113, '可爱小熊2', '小熊图案', 'TEXT_GENERATED', NULL, 'https://clothingpattern-1317715434.cos.ap-guangzhou.myqcloud.com/pattern/1991067449709658113/1764724744069.webp', 'https://clothingpattern-1317715434.cos.ap-guangzhou.myqcloud.com/pattern/1991067449709658113/1764724744069_thumbnail.png', 1690533, 'image/png', '可爱', '冬季', '儿童', NULL, 'APPROVED', NULL, NULL, NULL, 0, '2025-12-03 11:00:00', '2025-12-03 11:00:00', 0),
(2000000000000000011, 1991067449709658113, '可爱小熊3', '小熊图案', 'TEXT_GENERATED', NULL, 'https://clothingpattern-1317715434.cos.ap-guangzhou.myqcloud.com/pattern/1991067449709658113/1764724744069.webp', 'https://clothingpattern-1317715434.cos.ap-guangzhou.myqcloud.com/pattern/1991067449709658113/1764724744069_thumbnail.png', 1690533, 'image/png', '可爱', '冬季', '儿童', NULL, 'APPROVED', NULL, NULL, NULL, 0, '2025-12-04 10:00:00', '2025-12-04 10:00:00', 0),
(2000000000000000012, 1991067449709658113, '可爱小熊4', '小熊图案', 'TEXT_GENERATED', NULL, 'https://clothingpattern-1317715434.cos.ap-guangzhou.myqcloud.com/pattern/1991067449709658113/1764724744069.webp', 'https://clothingpattern-1317715434.cos.ap-guangzhou.myqcloud.com/pattern/1991067449709658113/1764724744069_thumbnail.png', 1690533, 'image/png', '可爱', '冬季', '儿童', NULL, 'APPROVED', NULL, NULL, NULL, 0, '2025-12-04 11:00:00', '2025-12-04 11:00:00', 0),

-- 2. 风格：简约（15条，过去一周）
(2000000000000000013, 1991067449709658113, '简约几何1', '几何图形', 'TEXT_GENERATED', NULL, 'https://clothingpattern-1317715434.cos.ap-guangzhou.myqcloud.com/pattern/1991067449709658113/1763950952460.webp', 'https://clothingpattern-1317715434.cos.ap-guangzhou.myqcloud.com/pattern/1991067449709658113/1763950952460_thumbnail.png', 1578480, 'image/png', '简约', '冬季', '成人', NULL, 'APPROVED', NULL, NULL, NULL, 0, '2025-11-28 15:00:00', '2025-11-28 15:00:00', 1),
(2000000000000000014, 1991067449709658113, '简约几何2', '几何图形', 'TEXT_GENERATED', NULL, 'https://clothingpattern-1317715434.cos.ap-guangzhou.myqcloud.com/pattern/1991067449709658113/1763951076212.webp', 'https://clothingpattern-1317715434.cos.ap-guangzhou.myqcloud.com/pattern/1991067449709658113/1763951076212_thumbnail.png', 1656872, 'image/png', '简约', '秋季', '青少年', NULL, 'APPROVED', NULL, NULL, NULL, 0, '2025-11-28 16:00:00', '2025-11-28 16:00:00', 1),
(2000000000000000015, 1994323049196937217, 1991067449709658113, 'MJ-简约几何图案-upsample2', '简约几何图案112', 'MJ_GENERATED', NULL, 'https://platform.cdn.zhishuyun.com/midjourney/7eed0d16-4aaf-4c79-8858-b4b4720a2bf0.png', 'https://platform.cdn.zhishuyun.com/midjourney/7eed0d16-4aaf-4c79-8858-b4b4720a2bf0.png?imageMogr2/thumbnail/!50p', NULL, NULL, '简约', '四季', '通用', '{\"task_id\": \"7eed0d16-4aaf-4c79-8858-b4b4720a2bf0\"}', 'APPROVED', '2025-11-29 17:04:10', 1991067449709658113, '管理员自动过审', 1, '2025-11-28 16:30:42', '2025-12-04 16:46:47', 0),
-- 补充简约风格剩余12条（简化示例）
(2000000000000000016, 1991067449709658113, '简约港风1', '港风碎花', 'TEXT_GENERATED', NULL, 'https://clothingpattern-1317715434.cos.ap-guangzhou.myqcloud.com/pattern/1991067449709658113/1763951196006.webp', 'https://clothingpattern-1317715434.cos.ap-guangzhou.myqcloud.com/pattern/1991067449709658113/1763951196006_thumbnail.png', 1898675, 'image/png', '简约', '四季', '青少年', NULL, 'APPROVED', NULL, NULL, NULL, 0, '2025-11-29 12:00:00', '2025-11-29 12:00:00', 1),
(2000000000000000017, 1991067449709658113, '简约港风2', '港风碎花', 'TEXT_GENERATED', NULL, 'https://clothingpattern-1317715434.cos.ap-guangzhou.myqcloud.com/pattern/1991067449709658113/1763951639824.webp', 'https://clothingpattern-1317715434.cos.ap-guangzhou.myqcloud.com/pattern/1991067449709658113/1763951639824_thumbnail.png', 2145002, 'image/png', '简约', '四季', '青少年', NULL, 'APPROVED', NULL, NULL, NULL, 0, '2025-11-29 13:00:00', '2025-11-29 13:00:00', 1),
(2000000000000000018, 1991067449709658113, '简约小狗1', '可爱的小狗', 'TEXT_GENERATED', NULL, 'https://clothingpattern-1317715434.cos.ap-guangzhou.myqcloud.com/pattern/1991067449709658113/1763951677108.webp', 'https://clothingpattern-1317715434.cos.ap-guangzhou.myqcloud.com/pattern/1991067449709658113/1763951677108_thumbnail.png', 1812459, 'image/png', '简约', '四季', '青少年', NULL, 'APPROVED', NULL, NULL, NULL, 0, '2025-11-29 14:00:00', '2025-11-29 14:00:00', 1),
(2000000000000000019, 1991067449709658113, '简约线条1', '线条图案', 'TEXT_GENERATED', NULL, 'https://clothingpattern-1317715434.cos.ap-guangzhou.myqcloud.com/pattern/1991067449709658113/1764724744069.webp', 'https://clothingpattern-1317715434.cos.ap-guangzhou.myqcloud.com/pattern/1991067449709658113/1764724744069_thumbnail.png', 1690533, 'image/png', '简约', '春季', '成人', NULL, 'APPROVED', NULL, NULL, NULL, 0, '2025-11-30 12:00:00', '2025-11-30 12:00:00', 0),
(2000000000000000020, 1991067449709658113, '简约线条2', '线条图案', 'TEXT_GENERATED', NULL, 'https://clothingpattern-1317715434.cos.ap-guangzhou.myqcloud.com/pattern/1991067449709658113/1764724744069.webp', 'https://clothingpattern-1317715434.cos.ap-guangzhou.myqcloud.com/pattern/1991067449709658113/1764724744069_thumbnail.png', 1690533, 'image/png', '简约', '春季', '成人', NULL, 'APPROVED', NULL, NULL, NULL, 0, '2025-11-30 13:00:00', '2025-11-30 13:00:00', 0),
(2000000000000000021, 1991067449709658113, '简约线条3', '线条图案', 'TEXT_GENERATED', NULL, 'https://clothingpattern-1317715434.cos.ap-guangzhou.myqcloud.com/pattern/1991067449709658113/1764724744069.webp', 'https://clothingpattern-1317715434.cos.ap-guangzhou.myqcloud.com/pattern/1991067449709658113/1764724744069_thumbnail.png', 1690533, 'image/png', '简约', '春季', '成人', NULL, 'APPROVED', NULL, NULL, NULL, 0, '2025-12-01 12:00:00', '2025-12-01 12:00:00', 0),
(2000000000000000022, 1991067449709658113, '简约线条4', '线条图案', 'TEXT_GENERATED', NULL, 'https://clothingpattern-1317715434.cos.ap-guangzhou.myqcloud.com/pattern/1991067449709658113/1764724744069.webp', 'https://clothingpattern-1317715434.cos.ap-guangzhou.myqcloud.com/pattern/1991067449709658113/1764724744069_thumbnail.png', 1690533, 'image/png', '简约', '春季', '成人', NULL, 'APPROVED', NULL, NULL, NULL, 0, '2025-12-01 13:00:00', '2025-12-01 13:00:00', 0),
(2000000000000000023, 1991067449709658113, '简约格子1', '格子图案', 'TEXT_GENERATED', NULL, 'https://clothingpattern-1317715434.cos.ap-guangzhou.myqcloud.com/pattern/1991067449709658113/1764724744069.webp', 'https://clothingpattern-1317715434.cos.ap-guangzhou.myqcloud.com/pattern/1991067449709658113/1764724744069_thumbnail.png', 1690533, 'image/png', '简约', '冬季', '成人', NULL, 'APPROVED', NULL, NULL, NULL, 0, '2025-12-02 12:00:00', '2025-12-02 12:00:00', 0),
(2000000000000000024, 1991067449709658113, '简约格子2', '格子图案', 'TEXT_GENERATED', NULL, 'https://clothingpattern-1317715434.cos.ap-guangzhou.myqcloud.com/pattern/1991067449709658113/1764724744069.webp', 'https://clothingpattern-1317715434.cos.ap-guangzhou.myqcloud.com/pattern/1991067449709658113/1764724744069_thumbnail.png', 1690533, 'image/png', '简约', '冬季', '成人', NULL, 'APPROVED', NULL, NULL, NULL, 0, '2025-12-02 13:00:00', '2025-12-02 13:00:00', 0),
(2000000000000000025, 1991067449709658113, '简约格子3', '格子图案', 'TEXT_GENERATED', NULL, 'https://clothingpattern-1317715434.cos.ap-guangzhou.myqcloud.com/pattern/1991067449709658113/1764724744069.webp', 'https://clothingpattern-1317715434.cos.ap-guangzhou.myqcloud.com/pattern/1991067449709658113/1764724744069_thumbnail.png', 1690533, 'image/png', '简约', '冬季', '成人', NULL, 'APPROVED', NULL, NULL, NULL, 0, '2025-12-03 12:00:00', '2025-12-03 12:00:00', 0),
(2000000000000000026, 1991067449709658113, '简约格子4', '格子图案', 'TEXT_GENERATED', NULL, 'https://clothingpattern-1317715434.cos.ap-guangzhou.myqcloud.com/pattern/1991067449709658113/1764724744069.webp', 'https://clothingpattern-1317715434.cos.ap-guangzhou.myqcloud.com/pattern/1991067449709658113/1764724744069_thumbnail.png', 1690533, 'image/png', '简约', '冬季', '成人', NULL, 'APPROVED', NULL, NULL, NULL, 0, '2025-12-03 13:00:00', '2025-12-03 13:00:00', 0),
(2000000000000000027, 1991067449709658113, '简约波点1', '波点图案', 'TEXT_GENERATED', NULL, 'https://clothingpattern-1317715434.cos.ap-guangzhou.myqcloud.com/pattern/1991067449709658113/1764724744069.webp', 'https://clothingpattern-1317715434.cos.ap-guangzhou.myqcloud.com/pattern/1991067449709658113/1764724744069_thumbnail.png', 1690533, 'image/png', '简约', '夏季', '成人', NULL, 'APPROVED', NULL, NULL, NULL, 0, '2025-12-04 12:00:00', '2025-12-04 12:00:00', 0),
(2000000000000000028, 1991067449709658113, '简约波点2', '波点图案', 'TEXT_GENERATED', NULL, 'https://clothingpattern-1317715434.cos.ap-guangzhou.myqcloud.com/pattern/1991067449709658113/1764724744069.webp', 'https://clothingpattern-1317715434.cos.ap-guangzhou.myqcloud.com/pattern/1991067449709658113/1764724744069_thumbnail.png', 1690533, 'image/png', '简约', '夏季', '成人', NULL, 'APPROVED', NULL, NULL, NULL, 0, '2025-12-04 13:00:00', '2025-12-04 13:00:00', 0),

-- 3. 风格：卡通（10条，过去一周）
(2000000000000000029, 1991067449709658113, '卡通水豚1', '水豚噜噜', 'TEXT_GENERATED', NULL, 'https://clothingpattern-1317715434.cos.ap-guangzhou.myqcloud.com/pattern/1991067449709658113/1763952240153.webp', 'https://clothingpattern-1317715434.cos.ap-guangzhou.myqcloud.com/pattern/1991067449709658113/1763952240153_thumbnail.png', 1360058, 'image/png', '卡通', '四季', '儿童', NULL, 'APPROVED', NULL, NULL, NULL, 0, '2025-11-28 17:00:00', '2025-12-04 16:46:47', 0),
(2000000000000000030, 1991067449709658113, '卡通水豚2', '水豚噜噜', 'TEXT_GENERATED', NULL, 'https://clothingpattern-1317715434.cos.ap-guangzhou.myqcloud.com/pattern/1991067449709658113/1763952668676.webp', 'https://clothingpattern-1317715434.cos.ap-guangzhou.myqcloud.com/pattern/1991067449709658113/1763952668676_thumbnail.png', 1269733, 'image/png', '卡通', '四季', '儿童', NULL, 'APPROVED', NULL, NULL, NULL, 0, '2025-11-28 18:00:00', '2025-12-04 16:46:47', 0),
-- 补充卡通风格剩余8条（简化示例）
(2000000000000000031, 1991067449709658113, '卡通水豚3', '水豚噜噜', 'TEXT_GENERATED', NULL, 'https://clothingpattern-1317715434.cos.ap-guangzhou.myqcloud.com/pattern/1991067449709658113/1764724744069.webp', 'https://clothingpattern-1317715434.cos.ap-guangzhou.myqcloud.com/pattern/1991067449709658113/1764724744069_thumbnail.png', 1690533, 'image/png', '卡通', '春季', '儿童', NULL, 'APPROVED', NULL, NULL, NULL, 0, '2025-11-29 15:00:00', '2025-11-29 15:00:00', 0),
(2000000000000000032, 1991067449709658113, '卡通水豚4', '水豚噜噜', 'TEXT_GENERATED', NULL, 'https://clothingpattern-1317715434.cos.ap-guangzhou.myqcloud.com/pattern/1991067449709658113/1764724744069.webp', 'https://clothingpattern-1317715434.cos.ap-guangzhou.myqcloud.com/pattern/1991067449709658113/1764724744069_thumbnail.png', 1690533, 'image/png', '卡通', '春季', '儿童', NULL, 'APPROVED', NULL, NULL, NULL, 0, '2025-11-29 16:00:00', '2025-11-29 16:00:00', 0),
(2000000000000000033, 1991067449709658113, '卡通恐龙1', '恐龙图案', 'TEXT_GENERATED', NULL, 'https://clothingpattern-1317715434.cos.ap-guangzhou.myqcloud.com/pattern/1991067449709658113/1764724744069.webp', 'https://clothingpattern-1317715434.cos.ap-guangzhou.myqcloud.com/pattern/1991067449709658113/1764724744069_thumbnail.png', 1690533, 'image/png', '卡通', '夏季', '儿童', NULL, 'APPROVED', NULL, NULL, NULL, 0, '2025-11-30 14:00:00', '2025-11-30 14:00:00', 0),
(2000000000000000034, 1991067449709658113, '卡通恐龙2', '恐龙图案', 'TEXT_GENERATED', NULL, 'https://clothingpattern-1317715434.cos.ap-guangzhou.myqcloud.com/pattern/1991067449709658113/1764724744069.webp', 'https://clothingpattern-1317715434.cos.ap-guangzhou.myqcloud.com/pattern/1991067449709658113/1764724744069_thumbnail.png', 1690533, 'image/png', '卡通', '夏季', '儿童', NULL, 'APPROVED', NULL, NULL, NULL, 0, '2025-11-30 15:00:00', '2025-11-30 15:00:00', 0),
(2000000000000000035, 1991067449709658113, '卡通汽车1', '汽车图案', 'TEXT_GENERATED', NULL, 'https://clothingpattern-1317715434.cos.ap-guangzhou.myqcloud.com/pattern/1991067449709658113/1764724744069.webp', 'https://clothingpattern-1317715434.cos.ap-guangzhou.myqcloud.com/pattern/1991067449709658113/1764724744069_thumbnail.png', 1690533, 'image/png', '卡通', '秋季', '儿童', NULL, 'APPROVED', NULL, NULL, NULL, 0, '2025-12-01 14:00:00', '2025-12-01 14:00:00', 0),
(2000000000000000036, 1991067449709658113, '卡通汽车2', '汽车图案', 'TEXT_GENERATED', NULL, 'https://clothingpattern-1317715434.cos.ap-guangzhou.myqcloud.com/pattern/1991067449709658113/1764724744069.webp', 'https://clothingpattern-1317715434.cos.ap-guangzhou.myqcloud.com/pattern/1991067449709658113/1764724744069_thumbnail.png', 1690533, 'image/png', '卡通', '秋季', '儿童', NULL, 'APPROVED', NULL, NULL, NULL, 0, '2025-12-01 15:00:00', '2025-12-01 15:00:00', 0),
(2000000000000000037, 1991067449709658113, '卡通星星1', '星星图案', 'TEXT_GENERATED', NULL, 'https://clothingpattern-1317715434.cos.ap-guangzhou.myqcloud.com/pattern/1991067449709658113/1764724744069.webp', 'https://clothingpattern-1317715434.cos.ap-guangzhou.myqcloud.com/pattern/1991067449709658113/1764724744069_thumbnail.png', 1690533, 'image/png', '卡通', '冬季', '儿童', NULL, 'APPROVED', NULL, NULL, NULL, 0, '2025-12-02 14:00:00', '2025-12-02 14:00:00', 0),
(2000000000000000038, 1991067449709658113, '卡通星星2', '星星图案', 'TEXT_GENERATED', NULL, 'https://clothingpattern-1317715434.cos.ap-guangzhou.myqcloud.com/pattern/1991067449709658113/1764724744069.webp', 'https://clothingpattern-1317715434.cos.ap-guangzhou.myqcloud.com/pattern/1991067449709658113/1764724744069_thumbnail.png', 1690533, 'image/png', '卡通', '冬季', '儿童', NULL, 'APPROVED', NULL, NULL, NULL, 0, '2025-12-02 15:00:00', '2025-12-02 15:00:00', 0),

-- 4. 风格：未来（8条，过去一周）
(2000000000000000039, 1994285805308223490, 1994692742118760449, 'MJ-简约几何线条-upsample2', '简约几何线条', 'MJ_GENERATED', NULL, 'https://platform.cdn.zhishuyun.com/midjourney/9d70480d-58a7-4191-8a46-8f0462a45455.png', 'https://platform.cdn.zhishuyun.com/midjourney/9d70480d-58a7-4191-8a46-8f0462a45455.png?imageMogr2/thumbnail/!50p', NULL, NULL, '未来', NULL, NULL, '{\"task_id\": \"9d70480d-58a7-4191-8a46-8f0462a45455\"}', 'APPROVED', NULL, NULL, NULL, 0, '2025-11-28 14:02:42', '2025-12-04 16:46:47', 0),
(2000000000000000040, 1995366367460564993, 1991067449709658113, '大黄狗11', '大黄狗', 'TEXT_GENERATED', NULL, 'https://clothingpattern-1317715434.cos.ap-guangzhou.myqcloud.com/pattern/1991067449709658113/1764567385962.webp', 'https://clothingpattern-1317715434.cos.ap-guangzhou.myqcloud.com/pattern/1991067449709658113/1764567385962_thumbnail.png', 2073275, 'image/png', '未来', '夏季', '通用', NULL, 'APPROVED', '2025-12-01 13:36:29', 1991067449709658113, '管理员自动过审', 0, '2025-12-01 13:36:28', '2025-12-03 09:29:42', 1),
(2000000000000000041, 1996442354772226049, 1991067449709658113, 'MJ-动物王国', '动物王国', 'MJ_GENERATED', NULL, 'https://platform.cdn.zhishuyun.com/midjourney/a3389a50-e7ff-44d0-b429-d41ccb07e218.png', 'https://platform.cdn.zhishuyun.com/midjourney/a3389a50-e7ff-44d0-b429-d41ccb07e218.png?imageMogr2/thumbnail/!50p', NULL, NULL, '未来', '四季', '中老年', '{\"task_id\": \"a3389a50-e7ff-44d0-b429-d41ccb07e218\"}', 'APPROVED', '2025-12-04 12:52:04', 1991067449709658113, '管理员自动过审', 0, '2025-12-04 12:52:04', '2025-12-04 12:52:04', 0),
-- 补充未来风格剩余5条（简化示例）
(2000000000000000042, 1991