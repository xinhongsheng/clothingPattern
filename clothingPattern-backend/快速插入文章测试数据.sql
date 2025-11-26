-- 快速插入文章测试数据
-- 使用方法：在MySQL中执行此SQL

-- 1. 先确保有分类数据
INSERT INTO article_category (id, category_name, category_desc, icon, sort_order, status, is_delete) VALUES
(1, '时尚资讯', '最新的时尚动态和流行趋势', '🎨', 100, 1, 0),
(2, '穿搭指南', '专业的穿搭技巧和建议', '👔', 90, 1, 0),
(3, '服装设计', '服装设计理念和创意分享', '✨', 80, 1, 0)
ON DUPLICATE KEY UPDATE category_name = category_name;

-- 2. 插入测试文章（已发布且已审核）
INSERT INTO article (
  id, category_id, title, cover_image, summary, content, 
  author, source, tags, view_count, like_count, comment_count, 
  share_count, collect_count, is_top, is_hot, is_recommend, 
  status, audit_status, publish_time, create_time, update_time, is_delete
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
<p>本季剪裁设计强调轮廓感，宽松的oversized款式与紧身的bodycon款式形成鲜明对比。</p>',
  '时尚编辑', '时尚周刊', '时尚,趋势,春夏', 
  1280, 156, 23, 45, 89, 
  1, 1, 1, 
  'PUBLISHED', 'APPROVED', NOW(), NOW(), NOW(), 0
),
(
  2, 2, '职场穿搭必备单品TOP10', 
  'https://picsum.photos/800/600?random=2',
  '为职场人士精选的10件必备单品，让你轻松打造专业形象...',
  '<h2>单品推荐</h2>
<p>1. <strong>经典白衬衫</strong>：永不过时的职场必备</p>
<p>2. <strong>黑色西装外套</strong>：正式场合的首选</p>
<p>3. <strong>A字半身裙</strong>：优雅又显瘦</p>',
  '穿搭顾问', '职场时尚', '职场,穿搭,单品', 
  856, 92, 15, 28, 67, 
  0, 1, 1, 
  'PUBLISHED', 'APPROVED', NOW(), NOW(), NOW(), 0
),
(
  3, 3, '极简主义设计理念在服装中的应用', 
  'https://picsum.photos/800/600?random=3',
  '探讨极简主义设计理念如何影响现代服装设计，以及如何在日常穿搭中运用极简风格...',
  '<h2>极简主义的核心</h2>
<p>极简主义强调"少即是多"的设计哲学，通过简洁的线条、纯净的色彩和精致的剪裁来表达设计理念。</p>
<h3>设计要素</h3>
<ul>
  <li>简洁的线条</li>
  <li>中性色调</li>
  <li>高质量面料</li>
  <li>精致的细节</li>
</ul>',
  '设计师张明', '设计杂志', '设计,极简,理念', 
  645, 78, 12, 18, 45, 
  0, 0, 1, 
  'PUBLISHED', 'APPROVED', NOW(), NOW(), NOW(), 0
)
ON DUPLICATE KEY UPDATE 
  title = VALUES(title),
  status = VALUES(status),
  audit_status = VALUES(audit_status);

-- 3. 验证数据
SELECT id, title, status, audit_status, is_delete FROM article WHERE is_delete = 0;

