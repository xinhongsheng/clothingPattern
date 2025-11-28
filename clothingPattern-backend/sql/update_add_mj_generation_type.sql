-- =============================================
-- Midjourney 功能数据库更新脚本
-- 更新时间：2025-11-28
-- 说明：为 pattern 表的 generationType 字段添加 MJ_GENERATED 类型支持
-- =============================================

-- 注意：MySQL 的 ENUM 类型需要修改列定义来添加新值
-- 如果你的 generationType 字段是 VARCHAR 类型，则不需要执行此脚本

-- 方案1：如果 generationType 是 VARCHAR 类型（推荐）
-- 无需执行任何 SQL，直接使用即可
-- 因为 VARCHAR 可以存储任意字符串值

-- 方案2：如果 generationType 是 ENUM 类型，需要执行以下 SQL
-- ALTER TABLE `pattern` 
-- MODIFY COLUMN `generationType` ENUM('TEXT_GENERATED', 'IMAGE_REFERENCED', 'MJ_GENERATED') 
-- CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL 
-- COMMENT '生成类型：TEXT_GENERATED（文字生成）、IMAGE_REFERENCED（图片参考生成）、MJ_GENERATED（Midjourney生成）';

-- =============================================
-- 验证查询
-- =============================================

-- 查看 generationType 字段的定义
-- SHOW FULL COLUMNS FROM `pattern` WHERE Field = 'generationType';

-- 查询所有 MJ 生成的图案
-- SELECT * FROM `pattern` WHERE generationType = 'MJ_GENERATED' ORDER BY createTime DESC;

-- 统计各类型图案数量
-- SELECT generationType, COUNT(*) as count FROM `pattern` GROUP BY generationType;

-- =============================================
-- 测试数据（可选）
-- =============================================

-- 插入一条测试数据（需要替换实际的 userId 和 URL）
/*
INSERT INTO `pattern` (
    `userId`, 
    `patternName`, 
    `description`, 
    `generationType`, 
    `patternUrl`, 
    `thumbUrl`, 
    `auditStatus`,
    `generationParams`
) VALUES (
    1, -- 替换为实际的用户ID
    'MJ-测试图案', 
    'The clothing pattern of the capybara Lulu', 
    'MJ_GENERATED', 
    'https://platform.cdn.zhishuyun.com/midjourney/test.png', 
    'https://platform.cdn.zhishuyun.com/midjourney/test.png?imageMogr2/thumbnail/!50p', 
    'PENDING',
    '{"taskId":"test-task-id","imageId":"test-image-id","success":true}'
);
*/

-- =============================================
-- 说明
-- =============================================

/*
根据你提供的建表语句，generationType 字段定义为：
`generationType` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL

这是 VARCHAR 类型，所以无需修改表结构，可以直接使用 'MJ_GENERATED' 值。

使用建议：
1. generationType 字段已支持存储 'MJ_GENERATED' 值
2. 代码中已添加 GenerationTypeEnum.MJ_GENERATED 枚举
3. 可以直接使用 /api/mj/generate 接口生成并保存图案
4. generationParams 字段（JSON类型）会保存完整的 MJ API 响应信息，包括：
   - taskId: 任务ID（用于后续操作）
   - imageId: 图片ID（用于后续操作）
   - actions: 可执行的动作列表
   - rawImageUrl: 原始图片URL
   - imageUrl: 缩略图URL
   - progress: 生成进度
   - 等等...

数据示例：
- patternName: "MJ-The clothing pattern of the ca"（自动截取前30字符）
- description: 完整的 prompt 提示词
- generationType: "MJ_GENERATED"
- patternUrl: 原始图片URL（高清大图）
- thumbUrl: 缩略图URL（列表展示用）
- auditStatus: "PENDING"（待审核）
- generationParams: 完整的 MJ API 响应 JSON
*/

