import crypto from 'node:crypto'
import fs from 'node:fs'
import path from 'node:path'

const outFile = path.resolve('clothingPattern-backend/sql/realistic_test_data_2026_0418_0501.sql')
const salt = 'xhs_!@#@#$<>M>MJDFIGSUIGFY'
const passwordHash = crypto.createHash('md5').update(salt + '12345678').digest('hex')

const dateList = [
  '2026-04-18',
  '2026-04-19',
  '2026-04-20',
  '2026-04-21',
  '2026-04-22',
  '2026-04-23',
  '2026-04-24',
  '2026-04-25',
  '2026-04-26',
  '2026-04-27',
  '2026-04-28',
  '2026-04-29',
  '2026-04-30',
  '2026-05-01',
]

const dateOrder = [0, 4, 9, 2, 12, 6, 1, 10, 5, 13, 3, 8, 11, 7]

const at = (index, hour = 9, minute = 0, second = 0) => {
  const day = dateList[dateOrder[index % dateOrder.length]]
  return `${day} ${String(hour).padStart(2, '0')}:${String(minute).padStart(2, '0')}:${String(second).padStart(2, '0')}`
}

const idRange = (prefix, count) => Array.from({ length: count }, (_, index) => prefix + BigInt(index + 1))

const extractTuples = (valuesSql) => {
  const tuples = []
  let start = -1
  let depth = 0
  let inString = false
  let escaped = false

  for (let index = 0; index < valuesSql.length; index += 1) {
    const char = valuesSql[index]

    if (inString) {
      if (escaped) {
        escaped = false
      } else if (char === '\\') {
        escaped = true
      } else if (char === "'") {
        inString = false
      }
      continue
    }

    if (char === "'") {
      inString = true
      continue
    }

    if (char === '(') {
      if (depth === 0) {
        start = index
      }
      depth += 1
    } else if (char === ')') {
      depth -= 1
      if (depth === 0 && start >= 0) {
        tuples.push(valuesSql.slice(start, index + 1))
        start = -1
      }
    }
  }

  return tuples
}

const findOriginalPatternBackupFile = () => {
  const candidates = [
    'C:/Users/19099/Downloads/xhs_clothingpattern_db_2026-04-19_00-17-11_mysql_data_7kKQZ.sql/xhs_clothingpattern_db_2026-04-19_00-17-11_mysql_data_7kKQZ.sql',
    'C:/Users/19099/Downloads/xhs_clothingpattern_db.sql',
  ]

  const found = candidates.find((candidate) => {
    try {
      return fs.statSync(candidate).isFile()
    } catch {
      return false
    }
  })

  if (!found) {
    throw new Error('未找到原始图案备份 SQL 文件')
  }

  return found
}

const readOriginalPatternData = () => {
  const backupFile = findOriginalPatternBackupFile()
  const backupSql = fs.readFileSync(backupFile, 'utf8')
  const insertStatements = [...backupSql.matchAll(/INSERT INTO `pattern` VALUES [\s\S]*?;/g)].map(
    (match) => match[0],
  )

  if (insertStatements.length === 0) {
    throw new Error(`备份文件中没有找到 pattern 表数据：${backupFile}`)
  }

  const tuples = insertStatements.flatMap((statement) => {
    const valuesSql = statement.replace(/^INSERT INTO `pattern` VALUES\s*/, '').replace(/;$/, '')
    return extractTuples(valuesSql)
  })

  const patternRecords = tuples.map((tuple) => {
    const match = tuple.match(/^\((\d+),(\d+),/)
    if (!match) {
      throw new Error(`无法解析 pattern 记录主键和用户 ID：${tuple.slice(0, 80)}`)
    }
    return {
      id: BigInt(match[1]),
      userId: BigInt(match[2]),
    }
  })

  return {
    backupFile,
    insertSql: insertStatements.join('\n'),
    patternIds: patternRecords.map((record) => record.id),
    ownerUserIds: [...new Set(patternRecords.map((record) => record.userId.toString()))].map((id) =>
      BigInt(id),
    ),
  }
}

const originalPatternData = readOriginalPatternData()
const generatedUserIds = idRange(900100000000000000n, 30)
const userIds = [...new Set([...originalPatternData.ownerUserIds, ...generatedUserIds].map(String))]
  .slice(0, 30)
  .map((id) => BigInt(id))
const patternIds = originalPatternData.patternIds.slice(-30)
const categoryIds = idRange(900300000000000000n, 24)
const articleIds = idRange(900400000000000000n, 30)
const commentIds = idRange(900500000000000000n, 30)

const q = (value) => {
  if (value === null || value === undefined) {
    return 'NULL'
  }
  if (typeof value === 'number') {
    return Number.isFinite(value) ? String(value) : 'NULL'
  }
  if (typeof value === 'bigint') {
    return value.toString()
  }
  return `'${String(value).replace(/\\/g, '\\\\').replace(/'/g, "''")}'`
}

const insert = (table, columns, rows) => {
  const values = rows
    .map((row) => `(${columns.map((column) => q(row[column])).join(', ')})`)
    .join(',\n')
  return `INSERT INTO \`${table}\` (${columns.map((column) => `\`${column}\``).join(', ')})\nVALUES\n${values};`
}

const provinces = [
  '广东',
  '浙江',
  '江苏',
  '四川',
  '北京',
  '上海',
  '山东',
  '河南',
  '湖北',
  '湖南',
  '福建',
  '江西',
  '安徽',
  '陕西',
  '重庆',
  '云南',
  '广西',
  '贵州',
  '河北',
  '辽宁',
  '天津',
  '山西',
  '内蒙古',
  '黑龙江',
  '吉林',
  '新疆',
  '甘肃',
  '海南',
  '青海',
  '宁夏',
]

const names = [
  '林若溪',
  '周安然',
  '顾念',
  '沈知夏',
  '唐予白',
  '许晴川',
  '叶青梧',
  '江屿',
  '宋南星',
  '程以宁',
  '陆嘉禾',
  '韩沐风',
  '苏晚晴',
  '秦越',
  '白景行',
  '温初见',
  '赵亦然',
  '何清欢',
  '夏知遥',
  '余星河',
  '魏明舒',
  '陈鹿鸣',
  '罗小满',
  '梁听雪',
  '马一禾',
  '袁栀子',
  '邱云舟',
  '姜月白',
  '董青禾',
  '黎书意',
]

const accountNames = [
  'admin',
  'zhou_anran',
  'gu_nian',
  'shen_zhixia',
  'tang_yubai',
  'xu_qingchuan',
  'ye_qingwu',
  'jiang_yu',
  'song_nanxing',
  'cheng_yining',
  'lu_jiahe',
  'han_mufeng',
  'su_wanqing',
  'qin_yue',
  'bai_jingxing',
  'wen_chujian',
  'zhao_yiran',
  'he_qinghuan',
  'xia_zhiyao',
  'yu_xinghe',
  'wei_mingshu',
  'chen_luming',
  'luo_xiaoman',
  'liang_tingxue',
  'ma_yihe',
  'yuan_zhizi',
  'qiu_yunzhou',
  'jiang_yuebai',
  'dong_qinghe',
  'li_shuyi',
]

const imageSeeds = [
  'qingci-floral',
  'ink-bamboo',
  'dunhuang-scarf',
  'sea-salt-stripe',
  'camellia-knit',
  'neon-active',
  'forest-fawn',
  'miao-geometry',
  'song-brocade',
  'cherry-dot',
  'rain-lane',
  'tropical-leaf',
  'snow-gradient',
  'checker-street',
  'moon-lotus',
  'campus-plaid',
  'grape-vine',
  'cyanotype-flower',
  'city-skyline',
  'handdrawn-cat',
  'butterfly-wing',
  'shell-wave',
  'grassland-horse',
  'coffee-apron',
  'ginkgo-coat',
  'mist-rose',
  'pixel-garden',
  'lattice-qipao',
  'night-run',
  'orange-blossom',
]

const tryOnTaskCodes = [
  'tryon_spring_azalea',
  'tryon_linen_commute',
  'tryon_resort_leaf',
  'tryon_studio_denim',
  'tryon_urban_runner',
  'tryon_morning_plaid',
  'tryon_soft_knit',
  'tryon_evening_silk',
  'tryon_campus_layer',
  'tryon_gallery_coat',
  'tryon_family_set',
  'tryon_weekend_cotton',
  'tryon_field_jacket',
  'tryon_tea_dress',
  'tryon_snow_parka',
  'tryon_city_shirt',
  'tryon_lake_skirt',
  'tryon_market_apron',
  'tryon_yoga_top',
  'tryon_tailored_blazer',
]

const fusionTaskCodes = [
  'fusion_celadon_bloom',
  'fusion_bamboo_shadow',
  'fusion_desert_silk',
  'fusion_blue_commute',
  'fusion_camellia_wool',
  'fusion_neon_motion',
  'fusion_fawn_story',
  'fusion_miao_patch',
  'fusion_cloud_brocade',
  'fusion_cherry_picnic',
  'fusion_gray_rain',
  'fusion_tropic_resort',
  'fusion_snow_peak',
  'fusion_mono_street',
  'fusion_lotus_home',
  'fusion_campus_check',
  'fusion_vine_scarf',
  'fusion_blueprint_flower',
  'fusion_skyline_hoodie',
  'fusion_cat_tee',
]

const users = userIds.map((id, index) => ({
  id,
  userAccount: accountNames[index],
  userPassword: passwordHash,
  userName: index === 0 ? '平台管理员' : names[index],
  userAvatar: `https://api.dicebear.com/8.x/adventurer/png?seed=${accountNames[index]}`,
  userProfile: `${provinces[index]}服装图案设计爱好者，关注AI纹样、面料趋势与穿搭灵感。`,
  province: provinces[index],
  userRole: index === 0 || index === 1 ? 'admin' : 'user',
  editTime: at(index, 10, index % 60),
  createTime: at(index, 9, index % 60),
  updateTime: at(index, 11, index % 60),
  isDelete: 0,
}))

const styleList = ['新中式', '国潮', '极简', '复古', '森系', '甜酷', '运动机能', '童趣', '民族风', '未来感']
const seasonList = ['春季', '夏季', '秋季', '冬季', '四季']
const audienceList = ['女装', '男装', '童装', '青年', '亲子', '职场', '运动户外']
const patternNames = [
  '青瓷花影连衣裙纹样',
  '水墨竹叶衬衫印花',
  '敦煌飞天丝巾图案',
  '海盐蓝条纹通勤图案',
  '山茶花针织提花',
  '赛博霓虹运动印花',
  '小鹿森林童装纹样',
  '苗绣几何夹克图案',
  '宋锦云纹半裙图案',
  '樱桃波点夏日图案',
  '雨巷灰调风衣纹样',
  '热带植物度假印花',
  '雪山渐变羽绒图案',
  '黑白棋盘街头图案',
  '荷塘月色睡衣纹样',
  '校园格纹背心图案',
  '葡萄藤蔓围巾纹样',
  '蓝晒花草环保印花',
  '城市天际线卫衣图案',
  '猫咪手绘T恤图案',
  '蝶翼渐变礼服纹样',
  '海浪贝壳亲子图案',
  '草原马纹民族图案',
  '咖啡豆工装围裙图案',
  '银杏叶秋日大衣纹样',
  '粉雾玫瑰婚纱纹样',
  '像素花园潮玩图案',
  '莲花窗棂旗袍图案',
  '星轨夜跑运动图案',
  '橙花果园家居服纹样',
]

const patterns = patternIds.map((id, index) => ({
  id,
  userId: userIds[index % userIds.length],
  patternName: patternNames[index],
  description: `用于${audienceList[index % audienceList.length]}系列的${styleList[index % styleList.length]}图案，适合上新企划、款式审核、用户点赞和个性化推荐场景。`,
  generationType: index % 3 === 0 ? 'TEXT_GENERATED' : index % 3 === 1 ? 'IMAGE_REFERENCED' : 'MJ_GENERATED',
  referenceImageUrl: index % 3 === 1 ? `https://picsum.photos/seed/ref-${imageSeeds[index]}/800/800` : null,
  patternUrl: `https://picsum.photos/seed/pattern-${imageSeeds[index]}/1024/1024`,
  thumbUrl: `https://picsum.photos/seed/thumb-${imageSeeds[index]}/480/480`,
  fileSize: 450000 + index * 13721,
  fileType: 'image/png',
  style: styleList[index % styleList.length],
  season: seasonList[index % seasonList.length],
  targetAudience: audienceList[index % audienceList.length],
  generationParams: JSON.stringify({
    size: '1024x1024',
    quality: 'high',
    prompt: `${patternNames[index]}，服装面料印花，高清细节`,
  }),
  auditStatus: index % 10 === 0 ? 'PENDING' : index % 13 === 0 ? 'REJECTED' : 'APPROVED',
  auditTime: index % 10 === 0 ? null : at(index, 15, index % 60),
  auditorId: index % 10 === 0 ? null : userIds[0],
  rejectReason: index % 13 === 0 ? '图案细节需要补充版权说明，暂不通过。' : null,
  likeCount: 12 + ((index * 7) % 88),
  createTime: at(index, 8 + (index % 8), index % 60),
  updateTime: at(index, 16 + (index % 5), index % 60),
  isDelete: 0,
}))

const categoryNames = [
  '潮流趋势',
  '面料百科',
  '图案灵感',
  'AI设计技巧',
  '色彩搭配',
  '传统纹样',
  '可持续时尚',
  '童装设计',
  '女装趋势',
  '男装趋势',
  '运动户外',
  '民族风格',
  '校园穿搭',
  '亲子系列',
  '婚礼礼服',
  '职场通勤',
  '电商运营',
  '设计师访谈',
  '工艺教程',
  '印花技术',
  '版型研究',
  '陈列搭配',
  '品牌案例',
  '行业资讯',
]

const categories = categoryIds.map((id, index) => ({
  id,
  categoryName: categoryNames[index],
  categoryDesc: `围绕${categoryNames[index]}整理的服装图案、流行趋势与设计方法，适合首页资讯栏目展示。`,
  icon: `https://api.iconify.design/mdi/${['hanger', 'palette', 'flower', 'robot', 'format-color-fill', 'leaf', 'tshirt-crew', 'school'][index % 8]}.svg`,
  sortOrder: 240 - index * 5,
  status: index % 11 === 0 ? 0 : 1,
  createTime: at(index, 9, 10 + index),
  updateTime: at(index, 18, 10 + index),
  isDelete: 0,
}))

const articleTitles = [
  '2026春夏女装印花趋势：清透花卉与低饱和色回归',
  '国潮纹样如何用于现代卫衣设计',
  'AI生成服装图案的提示词写法指南',
  '从宋锦云纹看中式礼服图案设计',
  '夏季度假系列常用热带植物元素解析',
  '童装图案设计的安全感与趣味表达',
  '可持续面料上的蓝晒印花实验',
  '通勤衬衫图案如何兼顾简洁和记忆点',
  '运动机能风图案的线条节奏',
  '苗绣几何元素在夹克中的再设计',
  '婚礼礼服中的玫瑰纹样层次设计',
  '校园格纹的色彩比例与搭配建议',
  '男装图案趋势：低调纹理与工装细节',
  '亲子装图案如何避免幼稚感',
  '电商主图如何突出服装图案卖点',
  '图案审核中的版权风险清单',
  '面料纹理对AI生成效果的影响',
  '服装图案收藏数据如何反推用户偏好',
  '新中式图案在年轻消费群体中的表达',
  '印花工艺选择：数码直喷还是热转印',
  '从评论区观察用户对图案的真实反馈',
  '小众品牌如何建立稳定的纹样资产库',
  '秋冬大衣图案的银杏叶灵感',
  '咖啡馆工装围裙的图案商业化案例',
  '夜跑运动服图案中的反光视觉设计',
  '像素风图案为何适合潮玩联名',
  '旗袍窗棂图案的现代比例重构',
  '服装内容运营如何提升图案转化率',
  '服装图案推荐系统的数据准备方法',
  '从浏览、点赞、收藏看用户画像构建',
]

const articles = articleIds.map((id, index) => ({
  id,
  categoryId: categoryIds[index % categoryIds.length],
  title: articleTitles[index],
  coverImage: `https://picsum.photos/seed/article-${imageSeeds[index]}/900/520`,
  summary: `本文围绕“${articleTitles[index]}”展开，结合服装图案设计、用户偏好和平台运营场景，提供用于内容运营、推荐分析和用户阅读的资讯内容。`,
  content: [
    `## ${articleTitles[index]}`,
    `这篇资讯用于沉淀服装图案趋势、设计方法和用户运营经验。内容重点关注${styleList[index % styleList.length]}风格与${audienceList[index % audienceList.length]}人群的匹配方式。`,
    `设计建议包括：先确定图案主题，再控制色彩数量，最后根据面料特性调整图案密度。对于需要AI辅助生成的场景，可以把“风格、元素、面料、使用场景、色彩倾向”写进提示词。`,
    `运营侧可以结合浏览量、点赞量、收藏量和评论量判断内容受欢迎程度，为后续图案推荐、报表分析和首页展示提供数据支撑。`,
  ].join('\n\n'),
  author: ['小红设计研究室', '织物趋势编辑部', 'AI纹样实验室', '服装图案观察'][index % 4],
  source: ['原创', '行业观察', '设计手记', '趋势报告'][index % 4],
  tags: [styleList[index % styleList.length], seasonList[index % seasonList.length], audienceList[index % audienceList.length]].join(','),
  viewCount: 180 + index * 37,
  likeCount: 16 + ((index * 5) % 65),
  commentCount: 3 + (index % 12),
  shareCount: 2 + (index % 9),
  collectCount: 5 + ((index * 3) % 42),
  isTop: index < 3 ? 1 : 0,
  isHot: index % 4 === 0 ? 1 : 0,
  isRecommend: index % 3 === 0 ? 1 : 0,
  status: index % 15 === 0 ? 'DRAFT' : 'PUBLISHED',
  auditStatus: index % 15 === 0 ? 'PENDING' : 'APPROVED',
  publishTime: index % 15 === 0 ? null : at(index, 12, index % 60),
  createTime: at(index, 10, index % 60),
  updateTime: at(index, 19, index % 60),
  isDelete: 0,
}))

const userLikes = patternIds.map((patternId, index) => ({
  id: 900600000000000001n + BigInt(index),
  userId: userIds[(index * 7) % userIds.length],
  patternId,
  createTime: at(index, 13, index % 60),
  isDelete: index % 17 === 0 ? 1 : 0,
  updateTime: at(index, 14, index % 60),
}))

const comments = commentIds.map((id, index) => {
  const isReply = index >= 20
  const rootIndex = index - 20
  return {
    id,
    userId: userIds[(index * 3 + 2) % userIds.length],
    patternId: patternIds[index % patternIds.length],
    content: isReply
      ? `回复：这个配色方案很适合当前图案，建议再做一个浅色版本，便于春夏款式复用。`
      : `这个图案的${styleList[index % styleList.length]}感觉很完整，适合用于${audienceList[index % audienceList.length]}系列展示。`,
    parentId: isReply ? commentIds[rootIndex] : null,
    rootId: isReply ? commentIds[rootIndex] : null,
    replyToUserId: isReply ? userIds[(rootIndex * 3 + 2) % userIds.length] : null,
    createTime: at(index, 15, index % 60),
    updateTime: at(index, 16, index % 60),
    isDelete: 0,
    likeCount: 2 + ((index * 2) % 20),
    replyCount: !isReply && index < 10 ? 1 : 0,
    topStatus: index < 2 ? 1 : 0,
    auditStatus: index % 18 === 0 ? 'PENDING' : 'APPROVED',
  }
})

const commentLikes = commentIds.map((commentId, index) => ({
  id: 900700000000000001n + BigInt(index),
  userId: userIds[(index * 5 + 1) % userIds.length],
  commentId,
  createTime: at(index, 17, index % 60),
  isDelete: index % 19 === 0 ? 1 : 0,
  patternId: comments[index].patternId,
}))

const articleLikes = articleIds.map((articleId, index) => ({
  id: 900800000000000001n + BigInt(index),
  userId: userIds[(index * 7 + 3) % userIds.length],
  articleId,
  createTime: at(index, 14, index % 60),
  isDelete: index % 20 === 0 ? 1 : 0,
}))

const articleCollects = articleIds.map((articleId, index) => ({
  id: 900900000000000001n + BigInt(index),
  userId: userIds[(index * 5 + 4) % userIds.length],
  articleId,
  createTime: at(index, 16, index % 60),
  isDelete: index % 23 === 0 ? 1 : 0,
}))

const banners = Array.from({ length: 20 }, (_, index) => ({
  id: 901000000000000001n + BigInt(index),
  title: [
    '春夏图案灵感周',
    'AI纹样生成大赛',
    '国潮新中式专题',
    '原创图案上新专区',
    '热门资讯精选',
  ][index % 5],
  imageUrl: `https://picsum.photos/seed/banner-fashion-${index + 1}/1440/480`,
  linkUrl: `/article/${articleIds[index % articleIds.length]}`,
  sortOrder: 200 - index * 5,
  status: index % 9 === 0 ? 0 : 1,
  createTime: at(index, 8, index % 60),
  updateTime: at(index, 18, index % 60),
  isDelete: 0,
}))

const tryOnTasks = Array.from({ length: 20 }, (_, index) => {
  const status = ['SUCCEEDED', 'SUCCEEDED', 'PENDING', 'FAILED'][index % 4]
  return {
    id: 901100000000000001n + BigInt(index),
    userId: userIds[index % userIds.length],
    personImageUrl: `https://picsum.photos/seed/person-${imageSeeds[index]}/768/1024`,
    topGarmentUrl: `https://picsum.photos/seed/top-${imageSeeds[index]}/768/1024`,
    bottomGarmentUrl: index % 3 === 0 ? null : `https://picsum.photos/seed/bottom-${imageSeeds[index]}/768/1024`,
    dashscopeTaskId: tryOnTaskCodes[index],
    taskStatus: status,
    resultImageUrl: status === 'SUCCEEDED' ? `https://picsum.photos/seed/tryon-${imageSeeds[index]}/768/1024` : null,
    createTime: at(index, 9, index % 60),
    updateTime: at(index, 17, index % 60),
    submitTime: at(index, 9, index % 60),
    scheduledTime: status === 'PENDING' ? null : at(index, 10, index % 60),
    endTime: status === 'SUCCEEDED' || status === 'FAILED' ? at(index, 11, index % 60) : null,
    errorCode: status === 'FAILED' ? 'IMAGE_EDGE_UNCLEAR' : null,
    errorMessage: status === 'FAILED' ? '服装边缘识别不完整，建议上传背景更干净的图片。' : null,
    localResultUrl: status === 'SUCCEEDED' ? `https://picsum.photos/seed/tryon-local-${imageSeeds[index]}/768/1024` : null,
  }
})

const imageFusionTasks = Array.from({ length: 20 }, (_, index) => {
  const status = ['SUCCEEDED', 'RUNNING', 'SUCCEEDED', 'FAILED', 'PENDING'][index % 5]
  return {
    id: 901200000000000001n + BigInt(index),
    userId: userIds[(index * 2) % userIds.length],
    dashscopeTaskId: fusionTaskCodes[index],
    prompt: `融合${styleList[index % styleList.length]}图案、面料纹理和服装场景，生成适合${audienceList[index % audienceList.length]}的展示图。`,
    negativePrompt: '低清晰度, 文字水印, 变形人体, 过度噪点',
    imageUrls: `https://picsum.photos/seed/fusion-a-${imageSeeds[index]}/768/768,https://picsum.photos/seed/fusion-b-${imageSeeds[index]}/768/768`,
    parameters: JSON.stringify({ size: '1024*1024', n: 2, seed: 20260418 + index }),
    taskStatus: status,
    submitTime: at(index, 10, index % 60),
    scheduledTime: status === 'PENDING' ? null : at(index, 11, index % 60),
    endTime: status === 'SUCCEEDED' || status === 'FAILED' ? at(index, 12, index % 60) : null,
    errorCode: status === 'FAILED' ? 'FUSION_STYLE_CONFLICT' : null,
    errorMessage: status === 'FAILED' ? '输入图片风格差异过大，未能稳定融合为同一服装视觉。' : null,
    origPrompts: `${styleList[index % styleList.length]}纹样,${seasonList[index % seasonList.length]}面料参考`,
    tempImageUrls: status === 'SUCCEEDED' ? `https://picsum.photos/seed/fusion-temp-${imageSeeds[index]}/1024/1024` : '',
    localImageUrls: status === 'SUCCEEDED' ? `https://picsum.photos/seed/fusion-local-${imageSeeds[index]}/1024/1024` : '',
    sorts: status === 'SUCCEEDED' ? '1' : '',
    createTime: at(index, 10, index % 60),
    updateTime: at(index, 18, index % 60),
  }
})

const userBehaviors = Array.from({ length: 30 }, (_, index) => {
  const actionType = ['VIEW', 'LIKE', 'DOWNLOAD'][index % 3]
  return {
    id: 901300000000000001n + BigInt(index),
    userId: userIds[(index * 4 + 1) % userIds.length],
    patternId: patternIds[(index * 5 + 2) % patternIds.length],
    actionType,
    weight: actionType === 'VIEW' ? 1 : actionType === 'DOWNLOAD' ? 3 : 5,
    createTime: at(index, 12, index % 60),
  }
})

const patternSimilarities = Array.from({ length: 30 }, (_, index) => {
  const patternIndexA = (index * 7 + 3) % patternIds.length
  const patternIndexB = (index * 11 + 14) % patternIds.length
  const similarity = [0.91, 0.76, 0.84, 0.69, 0.88, 0.73, 0.82, 0.67, 0.79, 0.86][index % 10]

  return {
    id: 901400000000000001n + BigInt(index),
    patternIdA: patternIds[patternIndexA],
    patternIdB: patternIds[patternIndexA === patternIndexB ? (patternIndexB + 5) % patternIds.length : patternIndexB],
    similarity,
    updateTime: at(index, 19, index % 60),
  }
})

const compatibilitySql = [
  '-- 兼容旧建表脚本中的驼峰表名，代码实际使用下划线表名。',
  'SET @schema_name = DATABASE();',
  '',
  "SET @old_exists = (SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = @schema_name AND table_name = 'userLike');",
  "SET @new_exists = (SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = @schema_name AND table_name = 'user_like');",
  "SET @sql = IF(@old_exists = 1 AND @new_exists = 0, 'RENAME TABLE `userLike` TO `user_like`', 'SELECT 1');",
  'PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;',
  '',
  "SET @old_exists = (SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = @schema_name AND table_name = 'articleCategory');",
  "SET @new_exists = (SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = @schema_name AND table_name = 'article_category');",
  "SET @sql = IF(@old_exists = 1 AND @new_exists = 0, 'RENAME TABLE `articleCategory` TO `article_category`', 'SELECT 1');",
  'PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;',
  '',
  "SET @old_exists = (SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = @schema_name AND table_name = 'articleLike');",
  "SET @new_exists = (SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = @schema_name AND table_name = 'article_like');",
  "SET @sql = IF(@old_exists = 1 AND @new_exists = 0, 'RENAME TABLE `articleLike` TO `article_like`', 'SELECT 1');",
  'PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;',
  '',
  "SET @old_exists = (SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = @schema_name AND table_name = 'articleCollect');",
  "SET @new_exists = (SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = @schema_name AND table_name = 'article_collect');",
  "SET @sql = IF(@old_exists = 1 AND @new_exists = 0, 'RENAME TABLE `articleCollect` TO `article_collect`', 'SELECT 1');",
  'PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;',
].join('\n')

const truncateSql = [
  'SET FOREIGN_KEY_CHECKS = 0;',
  'TRUNCATE TABLE `comment_like`;',
  'TRUNCATE TABLE `comment`;',
  'TRUNCATE TABLE `user_like`;',
  'TRUNCATE TABLE `article_like`;',
  'TRUNCATE TABLE `article_collect`;',
  'TRUNCATE TABLE `article`;',
  'TRUNCATE TABLE `article_category`;',
  'TRUNCATE TABLE `banner`;',
  'TRUNCATE TABLE `try_on_task`;',
  'TRUNCATE TABLE `image_fusion_task`;',
  'TRUNCATE TABLE `user_behavior`;',
  'TRUNCATE TABLE `pattern_similarity`;',
  'TRUNCATE TABLE `pattern`;',
  'TRUNCATE TABLE `user`;',
  'SET FOREIGN_KEY_CHECKS = 1;',
].join('\n')

const sections = [
  insert('user', ['id', 'userAccount', 'userPassword', 'userName', 'userAvatar', 'userProfile', 'province', 'userRole', 'editTime', 'createTime', 'updateTime', 'isDelete'], users),
  originalPatternData.insertSql,
  insert('article_category', ['id', 'categoryName', 'categoryDesc', 'icon', 'sortOrder', 'status', 'createTime', 'updateTime', 'isDelete'], categories),
  insert('article', ['id', 'categoryId', 'title', 'coverImage', 'summary', 'content', 'author', 'source', 'tags', 'viewCount', 'likeCount', 'commentCount', 'shareCount', 'collectCount', 'isTop', 'isHot', 'isRecommend', 'status', 'auditStatus', 'publishTime', 'createTime', 'updateTime', 'isDelete'], articles),
  insert('banner', ['id', 'title', 'imageUrl', 'linkUrl', 'sortOrder', 'status', 'createTime', 'updateTime', 'isDelete'], banners),
  insert('user_like', ['id', 'userId', 'patternId', 'createTime', 'isDelete', 'updateTime'], userLikes),
  insert('comment', ['id', 'userId', 'patternId', 'content', 'parentId', 'rootId', 'replyToUserId', 'createTime', 'updateTime', 'isDelete', 'likeCount', 'replyCount', 'topStatus', 'auditStatus'], comments),
  insert('comment_like', ['id', 'userId', 'commentId', 'createTime', 'isDelete', 'patternId'], commentLikes),
  insert('article_like', ['id', 'userId', 'articleId', 'createTime', 'isDelete'], articleLikes),
  insert('article_collect', ['id', 'userId', 'articleId', 'createTime', 'isDelete'], articleCollects),
  insert('try_on_task', ['id', 'userId', 'personImageUrl', 'topGarmentUrl', 'bottomGarmentUrl', 'dashscopeTaskId', 'taskStatus', 'resultImageUrl', 'createTime', 'updateTime', 'submitTime', 'scheduledTime', 'endTime', 'errorCode', 'errorMessage', 'localResultUrl'], tryOnTasks),
  insert('image_fusion_task', ['id', 'userId', 'dashscopeTaskId', 'prompt', 'negativePrompt', 'imageUrls', 'parameters', 'taskStatus', 'submitTime', 'scheduledTime', 'endTime', 'errorCode', 'errorMessage', 'origPrompts', 'tempImageUrls', 'localImageUrls', 'sorts', 'createTime', 'updateTime'], imageFusionTasks),
  insert('user_behavior', ['id', 'userId', 'patternId', 'actionType', 'weight', 'createTime'], userBehaviors),
  insert('pattern_similarity', ['id', 'patternIdA', 'patternIdB', 'similarity', 'updateTime'], patternSimilarities),
]

const summarySql = [
  'SELECT \'user\' AS tableName, COUNT(*) AS rowCount FROM `user`',
  'UNION ALL SELECT \'pattern\', COUNT(*) FROM `pattern`',
  'UNION ALL SELECT \'article_category\', COUNT(*) FROM `article_category`',
  'UNION ALL SELECT \'article\', COUNT(*) FROM `article`',
  'UNION ALL SELECT \'banner\', COUNT(*) FROM `banner`',
  'UNION ALL SELECT \'user_like\', COUNT(*) FROM `user_like`',
  'UNION ALL SELECT \'comment\', COUNT(*) FROM `comment`',
  'UNION ALL SELECT \'comment_like\', COUNT(*) FROM `comment_like`',
  'UNION ALL SELECT \'article_like\', COUNT(*) FROM `article_like`',
  'UNION ALL SELECT \'article_collect\', COUNT(*) FROM `article_collect`',
  'UNION ALL SELECT \'try_on_task\', COUNT(*) FROM `try_on_task`',
  'UNION ALL SELECT \'image_fusion_task\', COUNT(*) FROM `image_fusion_task`',
  'UNION ALL SELECT \'user_behavior\', COUNT(*) FROM `user_behavior`',
  'UNION ALL SELECT \'pattern_similarity\', COUNT(*) FROM `pattern_similarity`;',
].join('\n')

const sql = [
  '-- Clothing Pattern 业务模拟数据',
  '-- 日期范围：2026-04-18 至 2026-05-01',
  `-- pattern 表使用原始备份数据：${originalPatternData.backupFile.replace(/\\/g, '/')}`,
  '-- 使用方式：先执行 create_table.sql，再执行本文件。',
  '-- 测试账号：admin / 12345678，gu_nian / 12345678',
  '',
  'SET NAMES utf8mb4;',
  compatibilitySql,
  '',
  truncateSql,
  '',
  ...sections,
  '',
  '-- 导入后快速检查每张表的数据量',
  summarySql,
  '',
].join('\n\n')

fs.mkdirSync(path.dirname(outFile), { recursive: true })
fs.writeFileSync(outFile, sql, 'utf8')

const counts = {
  user: users.length,
  pattern: originalPatternData.patternIds.length,
  article_category: categories.length,
  article: articles.length,
  banner: banners.length,
  user_like: userLikes.length,
  comment: comments.length,
  comment_like: commentLikes.length,
  article_like: articleLikes.length,
  article_collect: articleCollects.length,
  try_on_task: tryOnTasks.length,
  image_fusion_task: imageFusionTasks.length,
  user_behavior: userBehaviors.length,
  pattern_similarity: patternSimilarities.length,
}

console.log(`Generated ${outFile}`)
console.log(JSON.stringify(counts, null, 2))
