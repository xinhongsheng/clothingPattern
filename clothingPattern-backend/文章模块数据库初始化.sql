-- =============================================
-- 文章资讯模块数据库初始化脚本
-- =============================================

-- 1. 创建文章分类表
CREATE TABLE IF NOT EXISTS `article_category` (
  `id` bigint NOT NULL COMMENT '分类ID',
  `category_name` varchar(100) NOT NULL COMMENT '分类名称',
  `category_desc` varchar(500) DEFAULT NULL COMMENT '分类描述',
  `icon` varchar(200) DEFAULT NULL COMMENT '分类图标',
  `sort_order` int DEFAULT '0' COMMENT '排序字段，越大越靠前',
  `status` int DEFAULT '1' COMMENT '状态：0-禁用，1-启用',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_delete` int DEFAULT '0' COMMENT '逻辑删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_category_name` (`category_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='文章分类表';

-- 2. 创建文章表
CREATE TABLE IF NOT EXISTS `article` (
  `id` bigint NOT NULL COMMENT '文章ID',
  `category_id` bigint DEFAULT NULL COMMENT '分类ID',
  `title` varchar(200) NOT NULL COMMENT '文章标题',
  `cover_image` varchar(500) DEFAULT NULL COMMENT '封面图',
  `summary` varchar(500) DEFAULT NULL COMMENT '文章摘要',
  `content` longtext COMMENT '文章内容',
  `author` varchar(100) DEFAULT NULL COMMENT '作者',
  `source` varchar(200) DEFAULT NULL COMMENT '来源',
  `tags` varchar(500) DEFAULT NULL COMMENT '标签，逗号分隔',
  `view_count` int DEFAULT '0' COMMENT '浏览量',
  `like_count` int DEFAULT '0' COMMENT '点赞量',
  `comment_count` int DEFAULT '0' COMMENT '评论量',
  `share_count` int DEFAULT '0' COMMENT '分享量',
  `collect_count` int DEFAULT '0' COMMENT '收藏量',
  `is_top` int DEFAULT '0' COMMENT '是否置顶：0-否，1-是',
  `is_hot` int DEFAULT '0' COMMENT '是否热门：0-否，1-是',
  `is_recommend` int DEFAULT '0' COMMENT '是否推荐：0-否，1-是',
  `status` varchar(20) DEFAULT 'DRAFT' COMMENT '状态：DRAFT-草稿，PUBLISHED-已发布，OFFLINE-已下架',
  `audit_status` varchar(20) DEFAULT 'PENDING' COMMENT '审核状态：PENDING-待审核，APPROVED-已通过，REJECTED-已拒绝',
  `publish_time` datetime DEFAULT NULL COMMENT '发布时间',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_delete` int DEFAULT '0' COMMENT '逻辑删除',
  PRIMARY KEY (`id`),
  KEY `idx_category_id` (`category_id`),
  KEY `idx_status` (`status`),
  KEY `idx_audit_status` (`audit_status`),
  KEY `idx_publish_time` (`publish_time`),
  KEY `idx_view_count` (`view_count`),
  KEY `idx_like_count` (`like_count`),
  KEY `idx_is_top` (`is_top`),
  KEY `idx_is_hot` (`is_hot`),
  KEY `idx_is_recommend` (`is_recommend`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='文章资讯表';

-- 3. 创建文章点赞表
CREATE TABLE IF NOT EXISTS `article_like` (
  `id` bigint NOT NULL COMMENT '点赞ID',
  `article_id` bigint NOT NULL COMMENT '文章ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_delete` int DEFAULT '0' COMMENT '逻辑删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_article_user` (`article_id`, `user_id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_article_id` (`article_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='文章点赞表';

-- 4. 创建文章收藏表
CREATE TABLE IF NOT EXISTS `article_collect` (
  `id` bigint NOT NULL COMMENT '收藏ID',
  `article_id` bigint NOT NULL COMMENT '文章ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_delete` int DEFAULT '0' COMMENT '逻辑删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_article_user` (`article_id`, `user_id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_article_id` (`article_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='文章收藏表';

-- =============================================
-- 插入测试数据
-- =============================================

-- 插入文章分类测试数据
INSERT INTO article_category (id, category_name, category_desc, icon, sort_order, status, is_delete) VALUES
(1, '时尚资讯', '最新的时尚动态和流行趋势', '🎨', 100, 1, 0),
(2, '穿搭指南', '专业的穿搭技巧和建议', '👔', 90, 1, 0),
(3, '服装设计', '服装设计理念和创意分享', '✨', 80, 1, 0),
(4, '面料知识', '各类面料的特性和选择', '🧵', 70, 1, 0),
(5, '品牌故事', '时尚品牌背后的故事', '🏢', 60, 1, 0),
(6, '时尚周报', '每周时尚新闻汇总', '📰', 50, 1, 0),
(7, '潮流预测', '未来时尚趋势预测分析', '🔮', 40, 1, 0);

-- 插入文章测试数据
INSERT INTO article (
  id, category_id, title, cover_image, summary, content, 
  author, source, tags, view_count, like_count, comment_count, 
  share_count, collect_count, is_top, is_hot, is_recommend, 
  status, audit_status, publish_time
) VALUES
(
  1, 1, '2025春夏时尚趋势解析：色彩与剪裁的完美融合', 
  'https://picsum.photos/800/600?random=1',
  '本文将为您详细解析2025年春夏季的时尚趋势，从色彩搭配到剪裁设计，全方位解读本季最值得关注的时尚元素...',
  '<h2>色彩趋势</h2>
<p>2025年春夏季，色彩趋势呈现出大胆而充满活力的特征。明亮的柠檬黄、清新的薄荷绿以及优雅的薰衣草紫成为本季主打色彩。</p>
<h3>主打色彩搭配</h3>
<p>1. <strong>柠檬黄 + 白色</strong>：清爽明快，适合日常穿搭</p>
<p>2. <strong>薄荷绿 + 裸色</strong>：温柔优雅，职场首选</p>
<p>3. <strong>薰衣草紫 + 灰色</strong>：高级感十足，晚宴必备</p>
<h2>剪裁设计</h2>
<p>本季剪裁设计强调轮廓感，宽松的oversized款式与紧身的bodycon款式形成鲜明对比。不规则下摆、斜肩设计以及夸张的泡泡袖成为设计师们的心头好。</p>
<h3>热门剪裁元素</h3>
<ul>
  <li>非对称裙摆</li>
  <li>泡泡袖设计</li>
  <li>露肩款式</li>
  <li>高腰线设计</li>
</ul>
<h2>面料选择</h2>
<p>轻薄透气的面料成为春夏季的首选。真丝、雪纺、亚麻等天然面料备受青睐，既舒适又优雅。</p>
<blockquote>
  <p>"时尚不仅仅是衣服，更是一种生活态度。" —— 香奈儿</p>
</blockquote>
<h2>搭配建议</h2>
<p>1. 清晨通勤：白色衬衫 + 高腰阔腿裤 + 柠檬黄手袋</p>
<p>2. 午后下午茶：薄荷绿连衣裙 + 裸色高跟鞋 + 精致项链</p>
<p>3. 晚宴派对：薰衣草紫礼服 + 银色配饰 + 优雅手包</p>',
  '时尚编辑部', '时尚杂志', '春夏趋势,色彩搭配,时尚解析', 
  3280, 456, 89, 123, 234, 1, 1, 1, 'PUBLISHED', 'APPROVED', NOW()
),
(
  2, 2, '职场穿搭必备单品TOP10：打造专业又时髦的形象', 
  'https://picsum.photos/800/600?random=2',
  '为职场人士精选的10件必备单品，让你轻松打造专业形象的同时不失时尚感。从基础款到点睛配饰，一应俱全...',
  '<h2>单品推荐清单</h2>
<h3>1. 经典白衬衫</h3>
<p>白衬衫是职场衣橱中最基础也最重要的单品。选择版型合身、面料挺括的款式，能让你的职场形象瞬间提升。</p>
<p><strong>推荐品牌：</strong>Uniqlo、ZARA、COS</p>
<h3>2. 黑色西装外套</h3>
<p>一件剪裁精良的黑色西装外套可以应对各种职场场合，从日常通勤到重要会议都游刃有余。</p>
<p><strong>选购建议：</strong>注意肩线和袖长，确保合身</p>
<h3>3. 高腰阔腿裤</h3>
<p>高腰设计拉长腿部线条，阔腿版型显瘦又舒适。深色系更显专业感。</p>
<h3>4. 膝盖长度铅笔裙</h3>
<p>经典优雅的铅笔裙是职场女性的必备单品，搭配衬衫或针织衫都很合适。</p>
<h3>5. 舒适通勤鞋</h3>
<p>一双舒适的低跟或中跟鞋对于需要长时间站立或走动的职场人士至关重要。</p>
<p><strong>推荐款式：</strong>尖头平底鞋、方跟单鞋、乐福鞋</p>
<h3>6. 质感手袋</h3>
<p>选择容量适中、质感良好的手袋，既能装下工作必需品，又能彰显个人品味。</p>
<h3>7. 简约手表</h3>
<p>一块简约大方的手表不仅实用，更是品味的象征。</p>
<h3>8. 基础款针织衫</h3>
<p>针织衫柔软舒适，是秋冬季节的绝佳选择。选择中性色更易搭配。</p>
<h3>9. 风衣外套</h3>
<p>春秋季节的通勤必备，既能防风保暖，又显干练气质。</p>
<h3>10. 精致配饰</h3>
<p>简约的项链、耳环或丝巾可以为整体造型画龙点睛。</p>
<h2>穿搭组合建议</h2>
<p><strong>方案一：</strong>白衬衫 + 黑色铅笔裙 + 低跟鞋 + 手提包</p>
<p><strong>方案二：</strong>针织衫 + 高腰阔腿裤 + 乐福鞋 + 风衣</p>
<p><strong>方案三：</strong>白衬衫 + 西装外套 + 西装裤 + 尖头鞋</p>',
  '穿搭顾问Anna', '职场时尚指南', '职场穿搭,必备单品,通勤装', 
  2156, 312, 67, 89, 178, 0, 1, 1, 'PUBLISHED', 'APPROVED', DATE_SUB(NOW(), INTERVAL 1 DAY)
),
(
  3, 3, '如何设计一件完美的连衣裙：从灵感到成品', 
  'https://picsum.photos/800/600?random=3',
  '从寻找设计灵感到最终成品，详细讲解连衣裙的设计全流程。包括版型选择、面料搭配、细节处理等关键环节...',
  '<h2>第一步：寻找设计灵感</h2>
<p>设计的第一步是寻找灵感。灵感可以来源于：</p>
<ul>
  <li>自然风景：花卉、海洋、山川</li>
  <li>艺术作品：绘画、雕塑、建筑</li>
  <li>街头时尚：观察路人的穿搭</li>
  <li>复古元素：历史服饰的重新演绎</li>
</ul>
<h2>第二步：确定设计风格</h2>
<p>根据目标客群和使用场景确定设计风格：</p>
<p>1. <strong>优雅复古风：</strong>适合晚宴或特殊场合</p>
<p>2. <strong>简约现代风：</strong>适合日常穿搭</p>
<p>3. <strong>甜美少女风：</strong>适合年轻女性</p>
<p>4. <strong>职业干练风：</strong>适合职场女性</p>
<h2>第三步：版型设计</h2>
<p>连衣裙的版型设计要考虑：</p>
<h3>领型选择</h3>
<p>• V领：拉长颈部线条，适合大多数脸型</p>
<p>• 圆领：甜美可爱，适合娇小身材</p>
<p>• 方领：复古优雅，适合锁骨明显的人</p>
<h3>袖型设计</h3>
<p>• 无袖：清爽利落，展现手臂线条</p>
<p>• 短袖：经典实用，四季适宜</p>
<p>• 长袖：优雅保守，适合正式场合</p>
<p>• 泡泡袖：甜美浪漫，增加设计感</p>
<h3>裙摆设计</h3>
<p>• A字型：显瘦百搭，适合梨形身材</p>
<p>• 直筒型：干练简约，适合职场</p>
<p>• 伞裙型：浪漫优雅，适合特殊场合</p>
<h2>第四步：面料选择</h2>
<p>不同季节和风格需要选择不同的面料：</p>
<p><strong>春夏季：</strong>雪纺、真丝、棉麻</p>
<p><strong>秋冬季：</strong>针织、呢绒、绒面</p>
<h2>第五步：细节处理</h2>
<p>细节决定成败，要注意：</p>
<ul>
  <li>拉链位置和类型</li>
  <li>纽扣款式和排列</li>
  <li>口袋设计（实用性vs装饰性）</li>
  <li>腰部处理（松紧带、系带、收腰）</li>
  <li>下摆处理（直边、荷叶边、不规则）</li>
</ul>
<h2>第六步：打版制作</h2>
<p>根据设计图纸进行打版，制作样衣进行试穿调整。</p>
<h2>设计心得</h2>
<blockquote>
  <p>"好的设计不仅要美观，更要实用。考虑穿着者的真实需求，才能设计出真正受欢迎的作品。"</p>
</blockquote>',
  '资深设计师Lisa', '服装设计学院', '服装设计,连衣裙,设计教程', 
  1845, 278, 45, 67, 156, 0, 0, 1, 'PUBLISHED', 'APPROVED', DATE_SUB(NOW(), INTERVAL 2 DAY)
),
(
  4, 4, '面料知识大全：如何选择适合的服装面料', 
  'https://picsum.photos/800/600?random=4',
  '全面介绍各类服装面料的特性、优缺点及适用场合，帮助你做出明智的面料选择...',
  '<h2>天然面料</h2>
<h3>1. 棉</h3>
<p><strong>特点：</strong>吸湿透气、柔软舒适、易染色</p>
<p><strong>优点：</strong>亲肤性好，适合敏感肌肤</p>
<p><strong>缺点：</strong>易皱、易缩水</p>
<p><strong>适用：</strong>T恤、衬衫、休闲装</p>
<h3>2. 真丝</h3>
<p><strong>特点：</strong>光泽柔和、手感滑爽、高级感强</p>
<p><strong>优点：</strong>舒适透气、吸湿性好</p>
<p><strong>缺点：</strong>易勾丝、保养较难</p>
<p><strong>适用：</strong>礼服、衬衫、丝巾</p>
<h3>3. 羊毛</h3>
<p><strong>特点：</strong>保暖性强、弹性好、不易起皱</p>
<p><strong>优点：</strong>冬季保暖首选</p>
<p><strong>缺点：</strong>易缩水、需专业清洗</p>
<p><strong>适用：</strong>大衣、西装、毛衣</p>
<h3>4. 亚麻</h3>
<p><strong>特点：</strong>透气清爽、天然质朴</p>
<p><strong>优点：</strong>夏季凉爽、环保健康</p>
<p><strong>缺点：</strong>极易起皱</p>
<p><strong>适用：</strong>夏季休闲装</p>
<h2>化学纤维</h2>
<h3>1. 涤纶（聚酯纤维）</h3>
<p><strong>特点：</strong>强度高、耐磨、不易变形</p>
<p><strong>优点：</strong>易打理、不易皱</p>
<p><strong>缺点：</strong>透气性差、易产生静电</p>
<p><strong>适用：</strong>运动装、工作服</p>
<h3>2. 锦纶（尼龙）</h3>
<p><strong>特点：</strong>轻薄耐磨、弹性好</p>
<p><strong>优点：</strong>快干、耐用</p>
<p><strong>缺点：</strong>易老化、不耐热</p>
<p><strong>适用：</strong>内衣、袜子、运动装</p>
<h3>3. 氨纶（莱卡）</h3>
<p><strong>特点：</strong>弹性极好</p>
<p><strong>优点：</strong>贴身舒适、不易变形</p>
<p><strong>缺点：</strong>不能单独使用</p>
<p><strong>适用：</strong>与其他面料混纺使用</p>
<h2>混纺面料</h2>
<p>混纺面料结合了多种纤维的优点，是现代服装的主流选择：</p>
<ul>
  <li><strong>棉+涤纶：</strong>既舒适又易打理</li>
  <li><strong>羊毛+涤纶：</strong>保暖且不易变形</li>
  <li><strong>棉+氨纶：</strong>舒适且有弹性</li>
</ul>
<h2>选购建议</h2>
<p>1. 根据季节选择：夏季选透气面料，冬季选保暖面料</p>
<p>2. 根据用途选择：运动装选速干面料，正装选挺括面料</p>
<p>3. 根据体质选择：敏感肌肤选天然面料</p>
<p>4. 注意成分标签：了解面料构成</p>
<p>5. 考虑护理难度：选择符合自己生活方式的面料</p>',
  '面料专家', '纺织技术杂志', '面料知识,材质选择,服装材料', 
  1567, 198, 34, 45, 123, 0, 0, 1, 'PUBLISHED', 'APPROVED', DATE_SUB(NOW(), INTERVAL 3 DAY)
),
(
  5, 5, '香奈儿品牌故事：从孤儿到时尚女王的传奇人生', 
  'https://picsum.photos/800/600?random=5',
  '探索可可·香奈儿的传奇人生，了解这位改变时尚历史的伟大女性如何创立世界顶级奢侈品牌...',
  '<h2>早年生活</h2>
<p>加布里埃·香奈儿（Gabrielle Chanel），1883年出生于法国。母亲去世后，她在孤儿院度过了童年。正是这段艰苦的经历，造就了她坚韧不拔的性格。</p>
<h2>事业起步</h2>
<p>1910年，香奈儿在巴黎开设了第一家女帽店。她设计的帽子简洁优雅，迅速赢得了上流社会的青睐。</p>
<h3>关键转折点</h3>
<ul>
  <li><strong>1913年：</strong>在多维尔开设时装店</li>
  <li><strong>1915年：</strong>推出针织运动装</li>
  <li><strong>1921年：</strong>发布标志性香水Chanel No.5</li>
  <li><strong>1926年：</strong>设计出经典"小黑裙"</li>
</ul>
<h2>设计理念</h2>
<p>香奈儿的设计理念彻底改变了女性时尚：</p>
<h3>1. 解放女性</h3>
<p>在香奈儿之前，女性穿着束腰、裙撑等束缚身体的服装。她提倡宽松、舒适的设计，让女性从繁复的装饰中解放出来。</p>
<h3>2. 简约优雅</h3>
<blockquote>
  <p>"简约是一切优雅的基调。" —— 可可·香奈儿</p>
</blockquote>
<p>她反对过度装饰，提倡简洁线条和中性色彩。</p>
<h3>3. 实用主义</h3>
<p>香奈儿认为服装首先要实用，然后才是美观。她设计的服装既优雅又便于日常活动。</p>
<h2>经典设计</h2>
<h3>小黑裙（Little Black Dress）</h3>
<p>1926年，香奈儿推出的小黑裙被《Vogue》杂志称为"香奈儿的福特"，意指它像福特汽车一样实用且人人都能穿。</p>
<h3>Chanel No.5香水</h3>
<p>这是第一款以数字命名的香水，也是第一款使用人工合成香料的香水。至今仍是全球最畅销的香水之一。</p>
<h3>2.55手袋</h3>
<p>1955年2月推出，因此得名"2.55"。这是第一款带有肩带的女士手袋，解放了女性的双手。</p>
<h3>斜纹软呢套装</h3>
<p>香奈儿标志性的套装设计，采用斜纹软呢面料，剪裁简洁，至今仍是时尚经典。</p>
<h2>经典语录</h2>
<ul>
  <li>"时尚易逝，风格永存。"</li>
  <li>"想要无可取代，就必须与众不同。"</li>
  <li>"华丽的反面不是贫穷，而是庸俗。"</li>
  <li>"20岁的脸是天生的，30岁的脸是生活雕刻的，50岁的脸是你自己选择的。"</li>
</ul>
<h2>品牌传承</h2>
<p>1971年香奈儿女士去世后，品牌经历了一段低谷期。1983年，卡尔·拉格斐接任创意总监，成功复兴了香奈儿品牌。</p>
<p>如今，香奈儿已成为世界顶级奢侈品牌，但始终保持着创始人的设计理念：简约、优雅、实用。</p>',
  '时尚史研究员', '时尚传记', '品牌故事,香奈儿,时尚历史', 
  2234, 389, 78, 112, 267, 1, 1, 1, 'PUBLISHED', 'APPROVED', DATE_SUB(NOW(), INTERVAL 4 DAY)
);

-- =============================================
-- 验证数据
-- =============================================

-- 查看分类数据
SELECT * FROM article_category WHERE is_delete = 0;

-- 查看文章数据
SELECT id, title, category_id, status, audit_status, view_count, like_count 
FROM article 
WHERE is_delete = 0 
ORDER BY is_top DESC, publish_time DESC;

-- 查看文章与分类关联
SELECT 
  a.id,
  a.title,
  ac.category_name,
  a.view_count,
  a.like_count,
  a.status,
  a.publish_time
FROM article a
LEFT JOIN article_category ac ON a.category_id = ac.id
WHERE a.is_delete = 0
ORDER BY a.is_top DESC, a.publish_time DESC;

