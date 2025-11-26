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