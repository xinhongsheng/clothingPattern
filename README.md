# 服装图案平台 (Clothing Pattern Platform)

一个基于 Vue 3 和 Spring Boot 的全栈服装图案分享与管理平台,提供 AI 虚拟试穿、图案展示、文章发布等功能。

## 📋 项目简介

本项目是一个现代化的服装图案平台,旨在为服装设计师和爱好者提供一个分享、浏览和管理服装图案的综合性平台。平台集成了 AI 技术,提供智能虚拟试穿功能,帮助用户更好地预览服装效果。

## ✨ 主要功能

- 🎨 **图案展示与浏览** - 浏览和搜索各类服装图案
- 🤖 **AI 虚拟试穿** - 基于 AI 的智能虚拟试穿功能
- 📝 **文章发布** - 支持 Markdown 编辑器的文章发布系统
- 📊 **数据分析** - 可视化数据展示与分析
- 💬 **评论互动** - 用户评论与互动功能
- 👤 **用户管理** - 完善的用户认证与权限管理

## 🛠️ 技术栈

### 前端 (clothingPattern-front)

- **框架**: Vue 3 + TypeScript
- **构建工具**: Vite
- **UI 组件库**: Ant Design Vue
- **状态管理**: Pinia
- **路由**: Vue Router
- **图表库**: ECharts
- **Markdown 编辑器**: md-editor-v3
- **数据可视化**: @kjgl77/datav-vue3
- **HTTP 客户端**: Axios

### 后端 (clothingPattern-backend)

- **框架**: Spring Boot 3.0.5
- **JDK**: Java 17
- **数据库**: MySQL
- **ORM**: MyBatis Plus 3.5.9
- **缓存**: Redis + Caffeine (本地缓存)
- **消息队列**: RabbitMQ (Spring AMQP)
- **API 文档**: Knife4j (OpenAPI 3)
- **对象存储**: 腾讯云 COS
- **AI 服务**: 
  - 阿里云 DashScope SDK (通义千问)
  - 火山引擎 豆包 SDK
- **工具库**: Hutool
- **其他**: Session 共享 (Spring Session Redis)

## 📁 项目结构

```
clothing-pattern/
├── clothingPattern-front/      # 前端项目目录
│   ├── src/
│   │   ├── api/                # API 接口定义
│   │   ├── components/         # 公共组件
│   │   ├── layout/             # 布局组件
│   │   ├── pages/              # 页面组件
│   │   ├── router/             # 路由配置
│   │   └── stores/             # Pinia 状态管理
│   └── package.json
│
├── clothingPattern-backend/    # 后端项目目录
│   ├── src/main/java/
│   │   └── com/xhs/clothingpatternbackend/
│   │       ├── controller/     # 控制器层
│   │       ├── service/        # 服务层
│   │       ├── mapper/         # 数据访问层
│   │       ├── model/          # 数据模型
│   │       ├── config/         # 配置类
│   │       └── sdk/            # 第三方 SDK 集成
│   └── pom.xml
│
└── README.md                   # 项目说明文档
```

## 🚀 快速开始

### 环境要求

- **Node.js**: v20.19.0 或 v22.12.0+
- **JDK**: 17
- **Maven**: 3.6+
- **MySQL**: 5.7+ 或 8.0+
- **Redis**: 5.0+
- **RabbitMQ**: 3.8+

### 前端启动

```bash
# 进入前端目录
cd clothingPattern-front

# 安装依赖
npm install

# 启动开发服务器
npm run dev

# 生产构建
npm run build
```

### 后端启动

```bash
# 进入后端目录
cd clothingPattern-backend

# 使用 Maven 构建
mvn clean install

# 运行 Spring Boot 应用
mvn spring-boot:run
```

## ⚙️ 配置说明

### 数据库配置

1. 创建 MySQL 数据库
2. 修改 `clothingPattern-backend/src/main/resources/application.yml` 中的数据库连接信息

### Redis 配置

修改 `application.yml` 中的 Redis 连接信息:

```yaml
spring:
  redis:
    host: localhost
    port: 6379
```

### 对象存储配置

配置腾讯云 COS 的访问密钥:

```yaml
cos:
  secretId: your-secret-id
  secretKey: your-secret-key
  region: your-region
  bucket: your-bucket
```

### AI 服务配置

配置阿里云 DashScope API Key 或火山引擎豆包 API Key

## 📖 API 文档

启动后端服务后,访问 Knife4j API 文档:

```
http://localhost:8080/doc.html
```

## 🔧 开发工具

### 前端开发命令

```bash
npm run dev          # 启动开发服务器
npm run build        # 生产构建
npm run preview      # 预览生产构建
npm run type-check   # TypeScript 类型检查
npm run lint         # 代码检查
npm run format       # 代码格式化
npm run openapi      # 生成 OpenAPI 客户端代码
```

## 📝 贡献指南

欢迎提交 Issue 和 Pull Request 来改进项目!

## 📄 许可证

本项目采用私有许可证,未经授权不得用于商业用途。

## 👥 联系方式

如有问题或建议,请通过以下方式联系我们:

- 提交 GitHub Issue
- 发送邮件至项目维护者

---

**注意**: 本项目仍在持续开发中,部分功能可能尚未完善。
