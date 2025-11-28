# Midjourney 超时问题解决方案

## 🐛 问题描述

调用 Midjourney API 时出现超时错误：

```
java.net.SocketTimeoutException: timeout
```

## 🔍 原因分析

### 1. Midjourney 生成时间较长
- Midjourney 生成图片通常需要 **30-120 秒**
- 复杂的提示词可能需要更长时间
- 服务器负载高时可能延迟

### 2. 原始超时设置不足
- 连接超时：30秒
- 读取超时：60秒 ❌（不够）
- 写入超时：30秒

## ✅ 解决方案

### 已实施的修复

#### 1. 增加超时时间

**文件**：`MJGenImage.java`

```java
public MJGenImage() {
    this.client = new OkHttpClient.Builder()
            .connectTimeout(60, TimeUnit.SECONDS)      // 连接超时60秒
            .readTimeout(180, TimeUnit.SECONDS)        // 读取超时180秒（3分钟）✅
            .writeTimeout(60, TimeUnit.SECONDS)        // 写入超时60秒
            .retryOnConnectionFailure(true)            // 连接失败时重试
            .build();
}
```

**改进**：
- ✅ 读取超时从 60秒 增加到 180秒（3分钟）
- ✅ 连接超时从 30秒 增加到 60秒
- ✅ 启用连接失败重试

#### 2. 优化错误处理

```java
try (Response response = client.newCall(httpRequest).execute()) {
    // ... 处理响应
} catch (SocketTimeoutException e) {
    log.error("Midjourney API调用超时，可能是生成时间过长或网络问题", e);
    throw new IOException("Midjourney API调用超时，图片生成时间较长，请稍后重试或检查网络连接", e);
}
```

**改进**：
- ✅ 捕获 SocketTimeoutException
- ✅ 提供友好的错误信息
- ✅ 给出明确的解决建议

#### 3. 添加日志提示

```java
log.info("提示：Midjourney生成图片通常需要30-120秒，请耐心等待...");
```

**改进**：
- ✅ 在调用前提示用户等待时间
- ✅ 帮助用户理解为什么需要等待

## 📊 超时时间对比

| 配置项 | 修改前 | 修改后 | 说明 |
|--------|--------|--------|------|
| 连接超时 | 30秒 | 60秒 | 建立连接的最长时间 |
| 读取超时 | 60秒 | **180秒** | 等待响应的最长时间 ⭐ |
| 写入超时 | 30秒 | 60秒 | 发送请求的最长时间 |
| 重试机制 | 无 | **启用** | 连接失败自动重试 ⭐ |

## 🔧 如果仍然超时怎么办？

### 方案1：进一步增加超时时间（推荐）

如果 180秒 仍然不够，可以继续增加：

```java
.readTimeout(300, TimeUnit.SECONDS)  // 5分钟
```

### 方案2：使用异步模式

将同步调用改为异步，避免阻塞：

```java
// 1. 提交任务，立即返回任务ID
// 2. 用户可以继续操作
// 3. 后台轮询任务状态
// 4. 完成后通知用户
```

### 方案3：检查网络连接

```bash
# 测试与 MJ API 的连接
curl -v https://api.zhishuyun.com/midjourney/imagine?token=your-token

# 检查 DNS 解析
nslookup api.zhishuyun.com

# 检查网络延迟
ping api.zhishuyun.com
```

### 方案4：联系 API 提供商

如果经常超时，可能是：
- API 服务器负载过高
- Token 配额不足
- 服务限流

## 💡 最佳实践建议

### 1. 前端提示用户

```vue
<el-button @click="generate" :loading="loading">
  {{ loading ? '生成中，预计需要1-2分钟...' : '生成图案' }}
</el-button>
```

### 2. 添加进度提示

```javascript
let dots = 0;
const interval = setInterval(() => {
  dots = (dots + 1) % 4;
  message.value = '正在生成图片' + '.'.repeat(dots);
}, 500);
```

### 3. 设置合理的用户预期

在页面上明确告知：
- ✅ "图片生成通常需要 1-2 分钟"
- ✅ "请耐心等待，不要关闭页面"
- ✅ "生成时间取决于服务器负载"

### 4. 实现重试机制

```java
public MJImagineResponse imagineWithRetry(MJImagineRequest request, int maxRetries) {
    int retries = 0;
    while (retries < maxRetries) {
        try {
            return imagine(request);
        } catch (SocketTimeoutException e) {
            retries++;
            if (retries >= maxRetries) {
                throw e;
            }
            log.warn("第{}次尝试超时，正在重试...", retries);
            Thread.sleep(5000); // 等待5秒后重试
        }
    }
}
```

### 5. 监控超时率

```java
// 记录超时次数
private AtomicInteger timeoutCount = new AtomicInteger(0);
private AtomicInteger totalCount = new AtomicInteger(0);

// 在 catch 块中
timeoutCount.incrementAndGet();

// 定期输出统计
log.info("超时率: {}/{} = {}%", 
    timeoutCount.get(), 
    totalCount.get(), 
    (timeoutCount.get() * 100.0 / totalCount.get())
);
```

## 📝 配置说明

### application.yml

```yaml
# Midjourney API 配置
mj:
  api:
    token: e93b4e9976d344a897ea34e0d99f87c1
    url: https://api.zhishuyun.com/midjourney/imagine
    # 注意：Midjourney生成图片通常需要30-120秒
    # 当前超时设置：连接60秒，读取180秒（3分钟），写入60秒
```

### 自定义超时时间（可选）

如果需要更灵活的配置，可以添加：

```yaml
mj:
  api:
    token: e93b4e9976d344a897ea34e0d99f87c1
    url: https://api.zhishuyun.com/midjourney/imagine
    timeout:
      connect: 60    # 连接超时（秒）
      read: 180      # 读取超时（秒）
      write: 60      # 写入超时（秒）
```

然后在代码中读取：

```java
@Value("${mj.api.timeout.connect:60}")
private int connectTimeout;

@Value("${mj.api.timeout.read:180}")
private int readTimeout;

@Value("${mj.api.timeout.write:60}")
private int writeTimeout;

public MJGenImage() {
    this.client = new OkHttpClient.Builder()
            .connectTimeout(connectTimeout, TimeUnit.SECONDS)
            .readTimeout(readTimeout, TimeUnit.SECONDS)
            .writeTimeout(writeTimeout, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)
            .build();
}
```

## 🎯 测试验证

### 1. 简单提示词测试

```bash
curl -X POST http://localhost:8123/api/mj/generate \
  -H "Content-Type: application/json" \
  -H "Cookie: JSESSIONID=your-session" \
  -d '{"prompt": "simple pattern"}'
```

**预期**：30-60秒内返回

### 2. 复杂提示词测试

```bash
curl -X POST http://localhost:8123/api/mj/generate \
  -H "Content-Type: application/json" \
  -H "Cookie: JSESSIONID=your-session" \
  -d '{"prompt": "highly detailed intricate clothing pattern with capybara, photorealistic, 8k, ultra detailed"}'
```

**预期**：60-120秒内返回

### 3. 观察日志

启动应用后，查看日志输出：

```
调用Midjourney API，请求参数：...
提示：Midjourney生成图片通常需要30-120秒，请耐心等待...
Midjourney API响应：...
```

## ⚠️ 注意事项

1. **不要设置过短的超时时间**
   - 最少应该设置 120秒（2分钟）
   - 推荐设置 180秒（3分钟）或更长

2. **考虑服务器资源**
   - 长时间等待会占用线程
   - 建议使用异步处理或增加线程池大小

3. **用户体验**
   - 明确告知用户等待时间
   - 提供取消操作的选项
   - 显示进度或动画

4. **错误处理**
   - 超时后给出明确的错误提示
   - 提供重试选项
   - 记录日志便于排查

## 📚 相关文档

- `Midjourney快速开始.md` - 快速入门
- `Midjourney接口使用说明.md` - API 文档
- `Midjourney保存图案使用指南.md` - 使用指南

## ✅ 修复总结

| 修改项 | 状态 |
|--------|------|
| 增加读取超时到180秒 | ✅ |
| 增加连接超时到60秒 | ✅ |
| 启用连接失败重试 | ✅ |
| 添加超时异常捕获 | ✅ |
| 优化错误提示信息 | ✅ |
| 添加等待时间日志 | ✅ |
| 更新配置文件说明 | ✅ |

**现在可以重新测试了！** 🚀

---

**更新时间**：2025-11-28  
**版本**：1.0

