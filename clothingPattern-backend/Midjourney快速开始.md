# Midjourney API 快速开始指南

## 🚀 快速启动

### 1. 启动项目
```bash
# 进入后端目录
cd clothingPattern-backend

# 启动Spring Boot应用
mvn spring-boot:run
```

### 2. 测试接口是否正常
打开浏览器或使用 curl 访问：
```
http://localhost:8123/api/mj/test
```

如果返回以下内容，说明服务正常：
```json
{
    "code": 0,
    "data": "Midjourney API服务正常",
    "message": "ok"
}
```

### 3. 生成你的第一张图片

#### 方法1: 使用 HTTP 文件测试（推荐）
1. 打开 `测试Midjourney功能.http` 文件
2. 点击 "生成图片（Imagine）" 请求旁边的运行按钮
3. 等待 30-60 秒，查看返回结果

#### 方法2: 使用 curl 命令
```bash
curl -X POST http://localhost:8123/api/mj/imagine \
  -H "Content-Type: application/json" \
  -d '{
    "prompt": "The clothing pattern of the capybara Lulu",
    "action": "generate"
  }'
```

#### 方法3: 使用 Postman 或其他 API 工具
- URL: `http://localhost:8123/api/mj/imagine`
- Method: `POST`
- Headers: `Content-Type: application/json`
- Body:
```json
{
    "prompt": "The clothing pattern of the capybara Lulu",
    "action": "generate"
}
```

### 4. 查看生成结果

成功响应示例：
```json
{
    "code": 0,
    "data": {
        "imageUrl": "https://platform.cdn.zhishuyun.com/midjourney/xxx.png?imageMogr2/thumbnail/!50p",
        "rawImageUrl": "https://platform.cdn.zhishuyun.com/midjourney/xxx.png",
        "imageWidth": 1024,
        "imageHeight": 1024,
        "rawImageWidth": 2048,
        "rawImageHeight": 2048,
        "taskId": "b1efab91-dda2-4dd7-b2fc-8694799beabf",
        "imageId": "1443792963657269248",
        "progress": 100,
        "success": true,
        "actions": [
            "upsample1", "upsample2", "upsample3", "upsample4",
            "reroll",
            "variation1", "variation2", "variation3", "variation4"
        ]
    },
    "message": "ok"
}
```

### 5. 对生成的图片进行操作

使用返回的 `taskId` 和 `imageId` 执行后续操作：

#### 放大某一张图（Upsample）
```bash
curl -X POST http://localhost:8123/api/mj/action \
  -H "Content-Type: application/json" \
  -d '{
    "taskId": "b1efab91-dda2-4dd7-b2fc-8694799beabf",
    "imageId": "1443792963657269248",
    "action": "upsample1"
  }'
```

#### 生成变体（Variation）
```bash
curl -X POST http://localhost:8123/api/mj/action \
  -H "Content-Type: application/json" \
  -d '{
    "taskId": "b1efab91-dda2-4dd7-b2fc-8694799beabf",
    "imageId": "1443792963657269248",
    "action": "variation2"
  }'
```

#### 重新生成（Reroll）
```bash
curl -X POST http://localhost:8123/api/mj/action \
  -H "Content-Type: application/json" \
  -d '{
    "taskId": "b1efab91-dda2-4dd7-b2fc-8694799beabf",
    "imageId": "1443792963657269248",
    "action": "reroll"
  }'
```

## 📝 提示词示例

### 服装图案相关
```
"The clothing pattern of the capybara Lulu"
"可爱的水豚图案，适合印在T恤上，卡通风格，简洁明快"
"Minimalist geometric pattern for t-shirt design"
"Floral pattern suitable for summer dress"
"Abstract art pattern for hoodie design"
```

### 提示词技巧
1. 描述要清晰具体
2. 可以指定风格：卡通、写实、抽象等
3. 可以指定用途：T恤、连衣裙、卫衣等
4. 可以指定颜色：明亮、柔和、黑白等
5. 支持中英文提示词

## 🔧 配置修改

如需修改 API Token 或 URL，编辑 `src/main/resources/application.yml`：

```yaml
# Midjourney API 配置
mj:
  api:
    token: 你的token
    url: https://api.zhishuyun.com/midjourney/imagine
```

## 📚 查看 Swagger 文档

启动项目后访问：
```
http://localhost:8123/api/doc.html
```

在 Swagger 中可以直接测试所有接口。

## ❓ 常见问题

### Q: 请求超时怎么办？
A: Midjourney 生成图片通常需要 30-60 秒，请耐心等待。如果经常超时，可以在 `MJGenImage` 类中增加超时时间。

### Q: 返回 success: false 怎么办？
A: 检查以下几点：
1. Token 是否正确
2. 网络连接是否正常
3. 提示词是否符合规范
4. 查看后端日志获取详细错误信息

### Q: 如何保存生成的图片？
A: 从响应中获取 `rawImageUrl`，然后：
1. 直接使用该 URL（图片存储在云端）
2. 下载到本地存储
3. 上传到自己的 COS/OSS

### Q: 可以批量生成吗？
A: 可以，但建议：
1. 控制并发数量（建议不超过 3 个并发）
2. 添加适当的延迟
3. 实现队列机制

## 📊 项目结构

```
clothingPattern-backend/
├── src/main/java/com/xhs/clothingpatternbackend/
│   ├── controller/
│   │   └── MJController.java          # API 控制器
│   ├── sdk/mj/
│   │   └── MJGenImage.java            # 核心服务类
│   └── model/dto/mj/
│       ├── MJImagineRequest.java      # 请求 DTO
│       ├── MJImagineResponse.java     # 响应 DTO
│       └── MJActionRequest.java       # 动作请求 DTO
├── src/main/resources/
│   └── application.yml                # 配置文件
├── 测试Midjourney功能.http            # HTTP 测试文件
├── Midjourney接口使用说明.md          # 详细文档
└── Midjourney快速开始.md              # 本文件
```

## 🎯 下一步

1. ✅ 测试基本功能
2. ✅ 尝试不同的提示词
3. ✅ 测试 upsample 和 variation 功能
4. 📝 集成到前端页面
5. 📝 添加图片历史记录
6. 📝 实现批量生成功能

## 💡 提示

- 生成的图片会返回 2x2 网格（4张候选图）
- 使用 `upsample` 可以放大选中的图片
- 使用 `variation` 可以基于选中的图片生成变体
- 所有操作都会记录日志，便于调试
- 建议先在测试环境验证功能

---

**祝你使用愉快！** 🎉

如有问题，请查看 `Midjourney接口使用说明.md` 获取更多详细信息。

