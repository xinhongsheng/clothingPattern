# Midjourney API 接口使用说明

## 功能概述

本项目已成功集成 Midjourney Imagine API，可以通过 RESTful 接口调用 Midjourney 生成图片。

## 配置信息

### 配置文件位置
`src/main/resources/application.yml`

### 配置内容
```yaml
# Midjourney API 配置
mj:
  api:
    token: e93b4e9976d344a897ea34e0d99f87c1
    url: https://api.zhishuyun.com/midjourney/imagine
```

## 核心类说明

### 1. MJGenImage 服务类
**位置**: `com.xhs.clothingpatternbackend.sdk.mj.MJGenImage`

**主要方法**:
- `imagine(MJImagineRequest request)`: 生成图片
- `executeAction(String taskId, String imageId, String action)`: 执行动作（如放大、变体等）

### 2. DTO 类

#### MJImagineRequest（请求参数）
```java
{
    "prompt": "提示词",
    "action": "generate"  // 默认值
}
```

#### MJImagineResponse（响应结果）
```java
{
    "imageUrl": "缩略图URL",
    "imageWidth": 1024,
    "imageHeight": 1024,
    "actions": ["upsample1", "upsample2", "upsample3", "upsample4", "reroll", "variation1", "variation2", "variation3", "variation4"],
    "rawImageUrl": "原始图片URL",
    "rawImageWidth": 2048,
    "rawImageHeight": 2048,
    "subImageUrls": [],
    "progress": 100,
    "imageId": "图片ID",
    "taskId": "任务ID",
    "success": true,
    "traceId": "追踪ID"
}
```

#### MJActionRequest（动作请求参数）
```java
{
    "taskId": "任务ID",
    "imageId": "图片ID",
    "action": "upsample1"  // 动作类型
}
```

### 3. MJController 控制器
**位置**: `com.xhs.clothingpatternbackend.controller.MJController`

**接口路径**: `/api/mj`

## API 接口文档

### 1. 测试接口
**请求**:
```
GET /api/mj/test
```

**响应**:
```json
{
    "code": 0,
    "data": "Midjourney API服务正常",
    "message": "ok"
}
```

### 2. 生成图片（Imagine）
**请求**:
```
POST /api/mj/imagine
Content-Type: application/json

{
    "prompt": "The clothing pattern of the capybara Lulu",
    "action": "generate"
}
```

**响应**:
```json
{
    "code": 0,
    "data": {
        "imageUrl": "https://platform.cdn.zhishuyun.com/midjourney/xxx.png?imageMogr2/thumbnail/!50p",
        "imageWidth": 1024,
        "imageHeight": 1024,
        "actions": ["upsample1", "upsample2", "upsample3", "upsample4", "reroll", "variation1", "variation2", "variation3", "variation4"],
        "rawImageUrl": "https://platform.cdn.zhishuyun.com/midjourney/xxx.png",
        "rawImageWidth": 2048,
        "rawImageHeight": 2048,
        "subImageUrls": [],
        "progress": 100,
        "imageId": "1443792963657269248",
        "taskId": "b1efab91-dda2-4dd7-b2fc-8694799beabf",
        "success": true,
        "traceId": "065e947f-9eb4-4f6e-80ff-8d822271d751"
    },
    "message": "ok"
}
```

### 3. 执行动作（Upsample/Variation/Reroll）
**请求**:
```
POST /api/mj/action
Content-Type: application/json

{
    "taskId": "b1efab91-dda2-4dd7-b2fc-8694799beabf",
    "imageId": "1443792963657269248",
    "action": "upsample1"
}
```

**支持的动作类型**:
- `upsample1` ~ `upsample4`: 放大第1~4张图
- `variation1` ~ `variation4`: 生成第1~4张图的变体
- `reroll`: 重新生成

**响应**: 同生成图片接口

## 使用示例

### Java 代码示例
```java
@Resource
private MJGenImage mjGenImage;

public void generateImage() {
    MJImagineRequest request = new MJImagineRequest();
    request.setPrompt("The clothing pattern of the capybara Lulu");
    request.setAction("generate");
    
    try {
        MJImagineResponse response = mjGenImage.imagine(request);
        if (response.getSuccess()) {
            System.out.println("图片URL: " + response.getRawImageUrl());
            System.out.println("任务ID: " + response.getTaskId());
            System.out.println("图片ID: " + response.getImageId());
        }
    } catch (IOException e) {
        e.printStackTrace();
    }
}
```

### HTTP 测试
使用 `测试Midjourney功能.http` 文件进行测试，该文件包含了所有接口的测试用例。

## 注意事项

1. **API Token**: 当前使用的 token 为 `e93b4e9976d344a897ea34e0d99f87c1`，如需更换请修改配置文件
2. **超时设置**: 连接超时30秒，读取超时60秒，写入超时30秒
3. **异常处理**: 所有网络异常都会被捕获并转换为 BusinessException
4. **日志记录**: 所有请求和响应都会记录日志，便于调试
5. **返回结果**: 生成图片是异步过程，但API会等待完成后返回结果（progress=100）

## 工作流程

1. **生成图片**: 调用 `/api/mj/imagine` 接口，传入提示词
2. **获取结果**: API 返回包含 4 张候选图的结果（2x2网格）
3. **选择操作**: 
   - 使用 `upsample` 放大某一张图
   - 使用 `variation` 基于某一张图生成变体
   - 使用 `reroll` 重新生成 4 张新图
4. **保存图片**: 从响应中获取 `rawImageUrl` 保存最终图片

## Swagger 文档

启动项目后，访问 Swagger 文档查看完整的 API 接口：
```
http://localhost:8123/api/doc.html
```

## 依赖说明

项目已添加以下依赖（在 `pom.xml` 中）：
```xml
<!-- OkHttp for Midjourney API -->
<dependency>
    <groupId>com.squareup.okhttp3</groupId>
    <artifactId>okhttp</artifactId>
    <version>4.12.0</version>
</dependency>

<!-- FastJSON2 for JSON parsing -->
<dependency>
    <groupId>com.alibaba.fastjson2</groupId>
    <artifactId>fastjson2</artifactId>
    <version>2.0.53</version>
</dependency>
```

## 常见问题

### Q1: 如何更换 API Token？
A: 修改 `application.yml` 中的 `mj.api.token` 配置项

### Q2: 生成图片需要多长时间？
A: 通常需要 30-60 秒，API 会等待生成完成后返回结果

### Q3: 如何处理生成失败？
A: 检查响应中的 `success` 字段，如果为 `false` 则生成失败，查看日志获取详细错误信息

### Q4: 可以并发生成多张图片吗？
A: 可以，但建议控制并发数量，避免超出 API 限制

## 后续扩展

可以考虑添加以下功能：
1. 异步任务队列，支持批量生成
2. 图片生成进度查询接口
3. 图片历史记录管理
4. 提示词模板管理
5. 图片自动保存到 COS

